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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerSyncTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void testPrintSynchronizedEndpoint() {

        Student student1 = new Student();
        student1.setName("Test Student 1");
        student1.setAge(20);
        studentRepository.save(student1);

        Student student2 = new Student();
        student2.setName("Test Student 2");
        student2.setAge(21);
        studentRepository.save(student2);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/student/print-synchronized",
                Map.class
        );

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("started", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("execution_time_ms"));

        Map<String, Object> requirements = (Map<String, Object>) response.getBody().get("requirements_met");
        assertNotNull(requirements);
        assertEquals(true, requirements.get("main_thread_first_two"));
        assertEquals(true, requirements.get("synchronized_method"));
    }

    @Test
    void testPrintSynchronizedWithNoStudents() {

        studentRepository.deleteAll();

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/student/print-synchronized",
                Map.class
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("started", response.getBody().get("status"));
    }
}