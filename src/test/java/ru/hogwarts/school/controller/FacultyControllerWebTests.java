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
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FacultyControllerWebTests {

    @LocalServerPort
    private int port;

    @Autowired
    private FacultyController facultyController;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private Faculty testFaculty;

    @BeforeEach
    void setUp() {

        facultyRepository.deleteAll();

        baseUrl = "http://localhost:" + port + "/faculty";

        testFaculty = new Faculty();
        testFaculty.setName("Гриффиндор");
        testFaculty.setColor("Красный");
    }

    @Test
    void contextLoad() throws Exception {
        Assertions.assertThat(facultyController).isNotNull();
    }

    @Test
    void testCreateFaculty() {
        ResponseEntity<Faculty> response = restTemplate.postForEntity(
                baseUrl,
                testFaculty,
                Faculty.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Гриффиндор", response.getBody().getName());
        assertEquals("Красный", response.getBody().getColor());
    }

    @Test
    void testGetFacultyInfo() {

        Faculty createdFaculty = restTemplate.postForObject(baseUrl, testFaculty, Faculty.class);

        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/" + createdFaculty.getId(),
                Faculty.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdFaculty.getId(), response.getBody().getId());
        assertEquals("Гриффиндор", response.getBody().getName());
    }

    @Test
    void testGetAllFaculties() {

        restTemplate.postForObject(baseUrl, testFaculty, Faculty.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl,
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testEditFaculty() {

        Faculty createdFaculty = restTemplate.postForObject(baseUrl, testFaculty, Faculty.class);

        createdFaculty.setName("Слизерин");
        createdFaculty.setColor("Зеленый");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Faculty> request = new HttpEntity<>(createdFaculty, headers);

        ResponseEntity<Faculty> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                request,
                Faculty.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Слизерин", response.getBody().getName());
        assertEquals("Зеленый", response.getBody().getColor());
    }

    @Test
    void testDeleteFaculty() {

        Faculty createdFaculty = restTemplate.postForObject(baseUrl, testFaculty, Faculty.class);

        restTemplate.delete(baseUrl + "/" + createdFaculty.getId());

        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/" + createdFaculty.getId(),
                Faculty.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetFacultiesByNameIgnoreCase() {

        restTemplate.postForObject(baseUrl, testFaculty, Faculty.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/name/гриффиндор", // lowercase
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetFacultiesByColorIgnoreCase() {

        restTemplate.postForObject(baseUrl, testFaculty, Faculty.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/color-ignore-case/красный", // lowercase
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testSearchFacultiesByNameOrColor() {

        restTemplate.postForObject(baseUrl, testFaculty, Faculty.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/search?nameOrColor=гриф", // частичное совпадение
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testSearchFacultiesByColor() {

        restTemplate.postForObject(baseUrl, testFaculty, Faculty.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/search?nameOrColor=крас", // частичное совпадение
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetFacultyWithStudents() {

        restTemplate.postForObject(baseUrl, testFaculty, Faculty.class);

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/faculty-students",
                Collection.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetFacultyNotFound() {

        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/999",
                Faculty.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testSearchFacultiesNotFound() {

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/search?nameOrColor=несуществующий",
                Collection.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetFacultiesByNameNotFound() {

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/name/несуществующий",
                Collection.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetFacultiesByColorNotFound() {

        ResponseEntity<Collection> response = restTemplate.getForEntity(
                baseUrl + "/color-ignore-case/несуществующий",
                Collection.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
