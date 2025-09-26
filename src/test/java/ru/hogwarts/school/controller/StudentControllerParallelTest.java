package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerParallelTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void testPrintParallelEndpoint() {

        createTestStudents(6);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/student/print-parallel",
                Map.class
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("started", response.getBody().get("status"));

        await().atMost(30, TimeUnit.SECONDS).until(() -> true);
    }

    @Test
    void testPrintParallelWithDifferentStudentCounts() {

        studentRepository.deleteAll();

        createTestStudents(2);
        ResponseEntity<Map> response1 = restTemplate.getForEntity(
                "http://localhost:" + port + "/student/print-parallel",
                Map.class
        );
        assertEquals(200, response1.getStatusCodeValue());

        studentRepository.deleteAll();
        createTestStudents(4);
        ResponseEntity<Map> response2 = restTemplate.getForEntity(
                "http://localhost:" + port + "/student/print-parallel",
                Map.class
        );
        assertEquals(200, response2.getStatusCodeValue());
    }

    private void createTestStudents(int count) {
        for (int i = 1; i <= count; i++) {
            Student student = new Student();
            student.setName("Test Student " + i);
            student.setAge(18 + i);
            studentRepository.save(student);
        }
    }
}