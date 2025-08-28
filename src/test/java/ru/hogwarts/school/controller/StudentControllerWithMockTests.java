package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
public class StudentControllerWithMockTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentService studentService;

    private final Student testStudent = new Student();

    {
        testStudent.setId(1L);
        testStudent.setName("Тестовый Студент");
        testStudent.setAge(20);
    }

    @Test
    void testGetStudentInfo() throws Exception {
        Mockito.when(studentService.findStudent(1L)).thenReturn(testStudent);

        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Тестовый Студент"))
                .andExpect(jsonPath("$.age").value(20));
    }

    @Test
    void testGetStudentInfoNotFound() throws Exception {
        Mockito.when(studentService.findStudent(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateStudent() throws Exception {
        Mockito.when(studentService.createStudent(any(Student.class))).thenReturn(testStudent);

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Тестовый Студент"));
    }

    @Test
    void testEditStudent() throws Exception {
        Student updatedStudent = new Student();
        updatedStudent.setId(1L);
        updatedStudent.setName("Обновленное Имя");
        updatedStudent.setAge(21);

        Mockito.when(studentService.editStudent(any(Student.class))).thenReturn(updatedStudent);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленное Имя"))
                .andExpect(jsonPath("$.age").value(21));
    }

    @Test
    void testEditStudentNotFound() throws Exception {
        Mockito.when(studentService.editStudent(any(Student.class))).thenReturn(null);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testStudent)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteStudent() throws Exception {
        Mockito.doNothing().when(studentService).deleteStudent(1L);

        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllStudents() throws Exception {
        Mockito.when(studentService.getAllStudents()).thenReturn(List.of(testStudent));

        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Тестовый Студент"));
    }

    @Test
    void testGetStudentsByAge() throws Exception {
        Mockito.when(studentService.findByAge(20)).thenReturn(List.of(testStudent));

        mockMvc.perform(get("/student/age/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].age").value(20));
    }

    @Test
    void testGetStudentsByAgeNotFound() throws Exception {
        Mockito.when(studentService.findByAge(99)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/student/age/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetStudentsByAgeRange() throws Exception {
        Mockito.when(studentService.findByAgeBetween(18, 25)).thenReturn(List.of(testStudent));

        mockMvc.perform(get("/student/age-between")
                        .param("minAge", "18")
                        .param("maxAge", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].age").value(20));
    }

    @Test
    void testGetStudentsByAgeRangeNotFound() throws Exception {
        Mockito.when(studentService.findByAgeBetween(30, 40)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/student/age-between")
                        .param("minAge", "30")
                        .param("maxAge", "40"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetStudentWithFaculty() throws Exception {
        Mockito.when(studentService.studentWithFaculty()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/student/student-faculty"))
                .andExpect(status().isOk());
    }
}
