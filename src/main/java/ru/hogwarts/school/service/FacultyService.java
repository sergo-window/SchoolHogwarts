package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.dto.FacultyWithStudents;
import ru.hogwarts.school.dto.StudentWithoutFaculty;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Collection;
import java.util.List;


@Service
public class FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);

    @Autowired
    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        logger.info("Was invoked method for create faculty: {}", faculty.getName());
        logger.debug("Creating faculty with details: name={}, color={}", faculty.getName(), faculty.getColor());

        try {
            if (faculty.getName() == null || faculty.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Faculty name cannot be null or empty");
            }

            if (faculty.getColor() == null) {
                throw new IllegalArgumentException("Faculty color must be indicated");
            }

            Faculty savedFaculty = facultyRepository.save(faculty);
            logger.info("Faculty created successfully with ID: {}", savedFaculty.getId());
            return savedFaculty;

        } catch (IllegalArgumentException e) {
            logger.error("Validation error when creating faculty: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error when creating faculty: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create faculty", e);
        }
    }

    public Faculty findFaculty(long id) {
        logger.info("Was invoked method for find faculty by ID: {}", id);
        logger.debug("Searching for faculty with ID: {}", id);

        Faculty faculty = facultyRepository.findById(id).orElse(null);

        if (faculty == null) {
            logger.warn("Faculty with ID {} not found", id);
        } else {
            logger.debug("Found faculty: {} (ID: {})", faculty.getName(), faculty.getId());
        }

        return faculty;
    }

    public Faculty editFaculty(Faculty faculty) {
        logger.info("Was invoked method for edit faculty with ID: {}", faculty.getId());
        logger.debug("Updating faculty: ID={}, name={}, color={}",
                faculty.getId(), faculty.getName(), faculty.getColor());

        Faculty updatedFaculty = facultyRepository.save(faculty);
        logger.info("Faculty with ID {} updated successfully", updatedFaculty.getId());

        return updatedFaculty;
    }

    public void deleteFaculty(long id) {
        logger.info("Was invoked method for delete faculty with ID: {}", id);

        if (!facultyRepository.existsById(id)) {
            logger.warn("Attempt to delete non-existent faculty with ID: {}", id);
            return;
        }

        facultyRepository.deleteById(id);
        logger.info("Faculty with ID {} deleted successfully", id);
    }

    public Collection<Faculty> getAllFaculties() {
        logger.info("Was invoked method for get all faculties");

        Collection<Faculty> faculties = facultyRepository.findAll();
        logger.debug("Retrieved {} faculties from database", faculties.size());

        if (faculties.isEmpty()) {
            logger.info("No faculties found in database");
        }

        return faculties;
    }

    public Collection<Faculty> findByNameIgnoreCase(String name) {
        logger.info("Was invoked method for find faculty by name (ignore case): {}", name);
        logger.debug("Searching for faculty with name: {}", name);

        Collection<Faculty> faculties = facultyRepository.findByNameIgnoreCase(name);
        logger.info("Found {} faculties with name: {}", faculties.size(), name);

        if (faculties.isEmpty()) {
            logger.debug("No faculties found with name: {}", name);
        }

        return faculties;
    }

    public Collection<Faculty> findByColorIgnoreCase(String color) {
        logger.info("Was invoked method for find faculty by color (ignore case): {}", color);
        logger.debug("Searching for faculty with color: {}", color);

        Collection<Faculty> faculties = facultyRepository.findByColorIgnoreCase(color);
        logger.info("Found {} faculties with color: {}", faculties.size(), color);

        if (faculties.isEmpty()) {
            logger.debug("No faculties found with color: {}", color);
        }

        return faculties;
    }

    public Collection<Faculty> findByNameOrColorIgnoreCase(String nameOrColor) {
        logger.info("Was invoked method for find faculty by name or color: {}", nameOrColor);
        logger.debug("Searching for faculty with name or color containing: {}", nameOrColor);

        Collection<Faculty> faculties = facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(nameOrColor, nameOrColor);

        logger.info("Found {} faculties matching '{}'", faculties.size(), nameOrColor);

        if (faculties.isEmpty()) {
            logger.debug("No faculties found matching: {}", nameOrColor);
        }

        return faculties;
    }

    public List<FacultyWithStudents> facultyWithStudents() {
        logger.info("Was invoked method for get all faculties with students information");

        try {
            List<Faculty> allFaculties = facultyRepository.findAll();
            logger.debug("Retrieved {} faculties from DB", allFaculties.size());

            List<FacultyWithStudents> result = allFaculties.stream()
                    .map(faculty -> {
                        logger.trace("Processing faculty: {} (ID: {})", faculty.getName(), faculty.getId());

                        List<StudentWithoutFaculty> students = faculty.getStudents().stream()
                                .map(student -> {
                                    logger.trace("Processing student: {} (ID: {})", student.getName(), student.getId());
                                    return new StudentWithoutFaculty(student.getId(), student.getName(), student.getAge());
                                })
                                .toList();

                        logger.debug("Faculty {} has {} students", faculty.getName(), students.size());
                        return new FacultyWithStudents(faculty.getId(), faculty.getName(), faculty.getColor(), students);
                    })
                    .toList();

            logger.info("Successfully processed {} faculties with student information", result.size());
            return result;

        } catch (Exception e) {
            logger.error("Error retrieving faculties with students: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve faculties with students", e);
        }
    }

    public String getLongestFacultyName() {
        logger.info("Was invoked method for get longest faculty name");

        return facultyRepository.findAll().stream()
                .map(Faculty::getName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .max(java.util.Comparator.comparingInt(String::length))
                .orElse("No faculties found");
    }
}