package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.dto.FacultyWithoutStudents;
import ru.hogwarts.school.dto.StudentWithFaculty;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.List;


@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        logger.info("Was invoked method for create student: {}", student.getName());
        logger.debug("Creating student with details: name={}, age={}", student.getName(), student.getAge());

        try {
            if (student.getName() == null || student.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Student name cannot be null or empty");
            }

            if (student.getAge() <= 0) {
                throw new IllegalArgumentException("Student age must be positive");
            }

            Student savedStudent = studentRepository.save(student);
            logger.info("Student created successfully with ID: {}", savedStudent.getId());
            return savedStudent;

        } catch (IllegalArgumentException e) {
            logger.error("Validation error when creating student: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error when creating student: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create student", e);
        }
    }

    public Student findStudent(long id) {
        logger.info("Was invoked method for find student by ID: {}", id);
        logger.debug("Searching for student with ID: {}", id);

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            logger.warn("Student with ID {} not found", id);
        } else {
            logger.debug("Found student: {} (ID: {})", student.getName(), student.getId());
        }

        return student;
    }

    public Student editStudent(Student student) {
        logger.info("Was invoked method for edit student with ID: {}", student.getId());
        logger.debug("Updating student: ID={}, name={}, age={}",
                student.getId(), student.getName(), student.getAge());

        Student updatedStudent = studentRepository.save(student);
        logger.info("Student with ID {} updated successfully", updatedStudent.getId());

        return updatedStudent;
    }

    public void deleteStudent(long id) {
        logger.info("Was invoked method for delete student with ID: {}", id);

        if (!studentRepository.existsById(id)) {
            logger.warn("Attempt to delete non-existent student with ID: {}", id);
            return;
        }

        studentRepository.deleteById(id);
        logger.info("Student with ID {} deleted successfully", id);
    }

    public Collection<Student> getAllStudents() {
        logger.info("Was invoked method for get all students");

        Collection<Student> students = studentRepository.findAll();
        logger.debug("Retrieved {} students from database", students.size());

        if (students.isEmpty()) {
            logger.info("No students found in database");
        }

        return students;
    }

    public Collection<Student> findByAge(int age) {
        logger.info("Was invoked method for find students by age: {}", age);
        logger.debug("Searching for students with age: {}", age);

        Collection<Student> students = studentRepository.findByAge(age);
        logger.info("Found {} students with age {}", students.size(), age);

        if (students.isEmpty()) {
            logger.debug("No students found with age: {}", age);
        }

        return students;
    }

    public Collection<Student> findByAgeBetween(int minAge, int maxAge) {
        logger.info("Was invoked method for find students by age range: {} - {}", minAge, maxAge);
        logger.debug("Searching for students between ages {} and {}", minAge, maxAge);

        Collection<Student> students = studentRepository.findByAgeBetween(minAge, maxAge);
        logger.info("Found {} students in age range {} - {}", students.size(), minAge, maxAge);

        if (students.isEmpty()) {
            logger.debug("No students found in age range {} - {}", minAge, maxAge);
        }

        return students;
    }

    public List<StudentWithFaculty> studentWithFaculty() {
        logger.info("Was invoked method for get students with faculty information");

        List<StudentWithFaculty> result = studentRepository.findAll().stream()
                .map(it -> {
                    Faculty faculty = it.getFaculty();
                    FacultyWithoutStudents facultyDto = faculty != null ?
                            new FacultyWithoutStudents(faculty.getId(), faculty.getName(), faculty.getColor()) :
                            null;
                    return new StudentWithFaculty(it.getId(), it.getName(), it.getAge(), facultyDto);
                })
                .toList();

        logger.debug("Processed {} students with faculty information", result.size());
        return result;
    }

    public int getCountStudents() {
        logger.info("Was invoked method for get total count of students");

        int count = studentRepository.getCountStudents();
        logger.info("Total students count: {}", count);

        return count;
    }

    public int getAverageAgeStudents() {
        logger.info("Was invoked method for get average age of students");

        int averageAge = studentRepository.getAverageAgeStudents();
        logger.info("Average age of students: {}", averageAge);

        return averageAge;
    }

    public List<Student> getLastFiveStudents() {
        logger.info("Was invoked method for get last five students");

        List<Student> lastFiveStudents = studentRepository.getLastFiveStudents();
        logger.debug("Retrieved last {} students", lastFiveStudents.size());

        if (lastFiveStudents.isEmpty()) {
            logger.info("No students found for last five query");
        }

        return lastFiveStudents;
    }

    public List<String> getStudentNamesStartingWithASorted() {
        logger.info("Was invoked method for get student names starting with 'A' (both Latin and Cyrillic)");

        return studentRepository.findAll().stream()
                .map(Student::getName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .filter(name -> {
                    String upperName = name.toUpperCase();
                    return upperName.startsWith("A") || upperName.startsWith("А");
                })
                .sorted()
                .map(String::toUpperCase)
                .toList();
    }

    public Double getAverageAgeWithStream() {
        logger.info("Was invoked method for get average age of students using Stream API");

        List<Student> allStudents = studentRepository.findAll();
        logger.debug("Processing {} students for average age calculation", allStudents.size());

        if (allStudents.isEmpty()) {
            logger.warn("No students found for average age calculation");
            return 0.0;
        }

        double averageAge = allStudents.stream()
                .filter(student -> student.getAge() > 0)
                .mapToInt(Student::getAge)
                .average()
                .orElse(0.0);

        logger.info("Average age calculated: {} (from {} students)", averageAge, allStudents.size());
        return averageAge;
    }
}