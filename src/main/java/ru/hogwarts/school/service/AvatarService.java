package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE_NEW;


@Service
@Transactional
public class AvatarService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);

    @Value("${path.to.avatars.folder}")
    private String avatarsDir;

    private final StudentService studentService;
    private final AvatarRepository avatarRepository;

    public AvatarService(StudentService studentService, AvatarRepository avatarRepository) {
        this.studentService = studentService;
        this.avatarRepository = avatarRepository;
        logger.info("AvatarService initialized with avatars directory: {}", avatarsDir);
    }

    public Avatar findAvatar(long studentId) {
        logger.info("Was invoked method for find avatar by student ID: {}", studentId);

        try {
            Avatar avatar = avatarRepository.findByStudentId(studentId).orElse(new Avatar());

            if (avatar.getId() == null) {
                logger.warn("Avatar not found for student ID: {}", studentId);
            } else {
                logger.debug("Found avatar for student ID {}: fileSize={} bytes, mediaType={}",
                        studentId, avatar.getFileSize(), avatar.getMediaType());
            }

            return avatar;

        } catch (Exception e) {
            logger.error("Error finding avatar for student ID {}: {}", studentId, e.getMessage(), e);
            throw new RuntimeException("Failed to find avatar", e);
        }
    }

    private byte[] generateImagePreview(Path filePath) throws IOException {
        logger.debug("Generating image preview for file: {}", filePath);
        long startTime = System.currentTimeMillis();

        try (InputStream is = Files.newInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(is, 1024);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                logger.error("Failed to read image file: {}", filePath);
                throw new IOException("Invalid image file");
            }

            int height = image.getHeight() / (image.getWidth() / 100);
            BufferedImage preview = new BufferedImage(100, height, image.getType());
            Graphics2D graphics = preview.createGraphics();
            graphics.drawImage(image, 0, 0, 100, height, null);
            graphics.dispose();

            String extension = getExtension(filePath.getFileName().toString());
            ImageIO.write(preview, extension, baos);

            byte[] previewData = baos.toByteArray();
            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Image preview generated in {} ms, size: {} bytes", executionTime, previewData.length);

            return previewData;

        } catch (IOException e) {
            logger.error("Error generating image preview for {}: {}", filePath, e.getMessage(), e);
            throw e;
        }
    }

    public void uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        logger.info("Was invoked method for upload avatar for student ID: {}", studentId);
        logger.debug("Uploading file: originalFilename={}, size={} bytes, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        if (file.isEmpty()) {
            logger.error("Attempt to upload empty file for student ID: {}", studentId);
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (!file.getContentType().startsWith("image/")) {
            logger.error("Invalid file type for student ID {}: {}", studentId, file.getContentType());
            throw new IllegalArgumentException("Only image files are allowed");
        }

        try {
            Student student = studentService.findStudent(studentId);
            if (student == null) {
                logger.error("Student not found with ID: {}", studentId);
                throw new IllegalArgumentException("Student not found with ID: " + studentId);
            }

            String extension = getExtension(file.getOriginalFilename());
            Path filePath = Path.of(avatarsDir, studentId + "." + extension);

            logger.debug("Saving avatar to path: {}", filePath);
            Files.createDirectories(filePath.getParent());
            Files.deleteIfExists(filePath);

            long fileWriteStart = System.currentTimeMillis();
            try (InputStream is = file.getInputStream();
                 OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
                 BufferedInputStream bis = new BufferedInputStream(is, 1024);
                 BufferedOutputStream bos = new BufferedOutputStream(os, 1024)) {
                bis.transferTo(bos);
            }
            long fileWriteTime = System.currentTimeMillis() - fileWriteStart;
            logger.debug("File saved to disk in {} ms", fileWriteTime);

            long dbOperationStart = System.currentTimeMillis();
            Avatar avatar = avatarRepository.findByStudentId(studentId).orElseGet(Avatar::new);
            avatar.setStudent(student);
            avatar.setFilePath(filePath.toString());
            avatar.setFileSize(file.getSize());
            avatar.setMediaType(file.getContentType());
            avatar.setData(generateImagePreview(filePath));

            Avatar savedAvatar = avatarRepository.save(avatar);
            long dbOperationTime = System.currentTimeMillis() - dbOperationStart;

            logger.info("Avatar uploaded successfully for student ID: {}. " +
                            "File: {} bytes, Preview: {} bytes. " +
                            "Total operation time: {} ms (file: {} ms, DB: {} ms)",
                    studentId, file.getSize(), savedAvatar.getData().length,
                    (fileWriteTime + dbOperationTime), fileWriteTime, dbOperationTime);

        } catch (IOException e) {
            logger.error("IO error during avatar upload for student ID {}: {}", studentId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during avatar upload for student ID {}: {}",
                    studentId, e.getMessage(), e);
            throw new RuntimeException("Failed to upload avatar", e);
        }
    }

    private String getExtension(String fileName) {
        try {
            if (fileName == null || !fileName.contains(".")) {
                logger.warn("Invalid file name for extension extraction: {}", fileName);
                return "jpg";
            }
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } catch (Exception e) {
            logger.error("Error extracting extension from fileName: {}", fileName, e);
            return "jpg";
        }
    }

    public List<Avatar> getAllAvatars(Integer pageNumber, Integer pageSize) {
        logger.info("Was invoked method for get all avatars. Page: {}, Size: {}", pageNumber, pageSize);

        try {
            if (pageNumber == null || pageNumber < 1) {
                logger.warn("Invalid page number: {}", pageNumber);
                pageNumber = 1;
            }
            if (pageSize == null || pageSize < 1) {
                logger.warn("Invalid page size: {}", pageSize);
                pageSize = 10;
            }

            PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);
            List<Avatar> avatars = avatarRepository.findAll(pageRequest).getContent();

            logger.info("Retrieved {} avatars from page {}", avatars.size(), pageNumber);
            logger.debug("Avatar IDs: {}",
                    avatars.stream()
                            .map(Avatar::getId)
                            .collect(java.util.stream.Collectors.toList()));

            return avatars;

        } catch (Exception e) {
            logger.error("Error retrieving avatars page {} with size {}: {}",
                    pageNumber, pageSize, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve avatars", e);
        }
    }
}
