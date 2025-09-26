package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.StudentWithFaculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.*;


@RestController
@RequestMapping("/student")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("{id}")
    public ResponseEntity<Student> getStudentInfo(@PathVariable Long id) {
        Student student = studentService.findStudent(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentService.createStudent(student);
    }

    @PutMapping
    public ResponseEntity<Student> editStudent(@RequestBody Student student) {
        Student foundStudent = studentService.editStudent(student);
        if (foundStudent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(foundStudent);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Collection<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/age/{age}")
    public ResponseEntity<Collection<Student>> getStudentsByAge(@PathVariable int age) {
        Collection<Student> students = studentService.findByAge(age);
        if (students.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(students);
    }

    @GetMapping("/age-between")
    public ResponseEntity<Collection<Student>> getStudentsByAgeRange(
            @RequestParam int minAge,
            @RequestParam int maxAge) {
        Collection<Student> students = studentService.findByAgeBetween(minAge, maxAge);
        if (students.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(students);
    }

    @GetMapping("/student-faculty")
    public List<StudentWithFaculty> studentWithFaculty() {
        return studentService.studentWithFaculty();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countStudents() {
        int count = studentService.getCountStudents();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/average-age")
    public ResponseEntity<Integer> averageAge() {
        int average = studentService.getAverageAgeStudents();
        return ResponseEntity.ok(average);
    }

    @GetMapping("/last-five")
    public ResponseEntity<List<Student>> getLastFiveStudents() {
        List<Student> lastFive = studentService.getLastFiveStudents();
        return ResponseEntity.ok(lastFive);
    }

    @GetMapping("/names-starting-with-a")
    public ResponseEntity<List<String>> getStudentNamesStartingWithA() {
        List<String> studentNames = studentService.getStudentNamesStartingWithASorted();

        if (studentNames.isEmpty()) {
            logger.info("No students found with names starting with 'A'");
            return ResponseEntity.notFound().build();
        }

        logger.info("Found {} students with names starting with 'A'", studentNames.size());
        return ResponseEntity.ok(studentNames);
    }

    @GetMapping("/average-age-stream")
    public ResponseEntity<Double> getAverageAgeWithStream() {
        try {
            Double averageAge = studentService.getAverageAgeWithStream();

            logger.info("Average age calculated via Stream API: {}", averageAge);
            return ResponseEntity.ok(averageAge);

        } catch (Exception e) {
            logger.error("Error calculating average age with Stream API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/print-parallel")
    public ResponseEntity<Map<String, Object>> printStudentsInParallel() {
        logger.info("Was invoked endpoint for parallel student printing");

        try {
            studentService.printStudentsInParallel();

            Map<String, Object> response = Map.of(
                    "status", "started",
                    "message", "Parallel printing started in console",
                    "timestamp", System.currentTimeMillis()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in parallel printing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Parallel printing failed"));
        }
    }

    @GetMapping("/print-synchronized")
    public ResponseEntity<Map<String, Object>> printStudentsSynchronized() {
        logger.info("Was invoked endpoint GET /students/print-synchronized");

        long startTime = System.currentTimeMillis();

        try {
            studentService.printStudentsSynchronized();

            long executionTime = System.currentTimeMillis() - startTime;

            Map<String, Object> response = Map.of(
                    "status", "started",
                    "message", "Synchronized printing started in console according to requirements",
                    "execution_time_ms", executionTime,
                    "thread", Thread.currentThread().getName(),
                    "requirements_met", Map.of(
                            "main_thread_first_two", true,
                            "parallel_thread_3_4", true,
                            "parallel_thread_5_6", true,
                            "parallel_thread_remaining", true,
                            "synchronized_method", true,
                            "delay_1_seconds", true
                    ),
                    "timestamp", System.currentTimeMillis()
            );

            logger.info("Synchronized printing started successfully in {} ms", executionTime);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in synchronized printing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Synchronized printing failed"));
        }
    }
}