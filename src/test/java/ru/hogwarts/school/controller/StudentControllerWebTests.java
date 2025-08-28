package ru.hogwarts.school.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerWebTests {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentController studentController;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private Student testStudent;

    @BeforeEach
    void setUp() {

        studentRepository.deleteAll();

        baseUrl = "http://localhost:" + port + "/student";

        testStudent = new Student();
        testStudent.setName("Тестовый Студент");
        testStudent.setAge(20);
    }

    @Test
    void contextLoad() throws Exception {
        Assertions.assertThat(studentController).isNotNull();
    }

    @Test
    void testCreateStudent() {
        ResponseEntity<Student> response = restTemplate.postForEntity(
                baseUrl,
                testStudent,
                Student.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Тестовый Студент", response.getBody().getName());
        assertEquals(20, response.getBody().getAge());
    }

    @Test
    void testGetStudentInfo() {

        Student createdStudent = restTemplate.postForObject(baseUrl, testStudent, Student.class);

        ResponseEntity<Student> response = restTemplate.getForEntity(
                baseUrl + "/" + createdStudent.getId(),
                Student.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdStudent.getId(), response.getBody().getId());
        assertEquals("Тестовый Студент", response.getBody().getName());
    }

    @Test
    void testGetAllStudents() {

        restTemplate.postForObject(baseUrl, testStudent, Student.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl,
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testEditStudent() {

        Student createdStudent = restTemplate.postForObject(baseUrl, testStudent, Student.class);

        createdStudent.setName("Обновленное Имя");
        createdStudent.setAge(21);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Student> request = new HttpEntity<>(createdStudent, headers);

        ResponseEntity<Student> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                Student.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Обновленное Имя", response.getBody().getName());
        assertEquals(21, response.getBody().getAge());
    }

    @Test
    void testDeleteStudent() {

        Student createdStudent = restTemplate.postForObject(baseUrl, testStudent, Student.class);

        restTemplate.delete(baseUrl + "/" + createdStudent.getId());

        ResponseEntity<Student> response = restTemplate.getForEntity(
                baseUrl + "/" + createdStudent.getId(),
                Student.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetStudentsByAge() {

        restTemplate.postForObject(baseUrl, testStudent, Student.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/age/20",
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetStudentsByAgeRange() {

        restTemplate.postForObject(baseUrl, testStudent, Student.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/age-between?minAge=18&maxAge=25",
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetStudentWithFaculty() {

        Faculty faculty = new Faculty();
        faculty.setName("Гриффиндор");
        faculty.setColor("Красный");
        Faculty createdFaculty = restTemplate.postForObject(
                "http://localhost:" + port + "/faculty",
                faculty,
                Faculty.class
        );

        testStudent.setFaculty(createdFaculty);
        restTemplate.postForObject(baseUrl, testStudent, Student.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/student-faculty",
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetStudentNotFound() {
        ResponseEntity<Student> response = restTemplate.getForEntity(
                baseUrl + "/9999",
                Student.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
