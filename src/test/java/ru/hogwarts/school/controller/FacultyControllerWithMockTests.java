package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.service.FacultyService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
public class FacultyControllerWithMockTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FacultyService facultyService;

    private final Faculty testFaculty = new Faculty();

    {
        testFaculty.setId(1L);
        testFaculty.setName("Гриффиндор");
        testFaculty.setColor("Красный");
    }

    @Test
    void testGetFacultyInfo() throws Exception {
        Mockito.when(facultyService.findFaculty(1L)).thenReturn(testFaculty);

        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гриффиндор"))
                .andExpect(jsonPath("$.color").value("Красный"));
    }

    @Test
    void testGetFacultyInfoNotFound() throws Exception {
        Mockito.when(facultyService.findFaculty(999L)).thenReturn(null);

        mockMvc.perform(get("/faculty/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateFaculty() throws Exception {
        Mockito.when(facultyService.createFaculty(any(Faculty.class))).thenReturn(testFaculty);

        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFaculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гриффиндор"));
    }

    @Test
    void testEditFaculty() throws Exception {
        Faculty updatedFaculty = new Faculty();
        updatedFaculty.setId(1L);
        updatedFaculty.setName("Слизерин");
        updatedFaculty.setColor("Зеленый");

        Mockito.when(facultyService.editFaculty(any(Faculty.class))).thenReturn(updatedFaculty);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFaculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Слизерин"))
                .andExpect(jsonPath("$.color").value("Зеленый"));
    }

    @Test
    void testEditFacultyNotFound() throws Exception {
        Mockito.when(facultyService.editFaculty(any(Faculty.class))).thenReturn(null);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFaculty)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteFaculty() throws Exception {
        Mockito.doNothing().when(facultyService).deleteFaculty(1L);

        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllFaculties() throws Exception {
        Mockito.when(facultyService.getAllFaculties()).thenReturn(List.of(testFaculty));

        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Гриффиндор"));
    }

    @Test
    void testGetFacultiesByNameIgnoreCase() throws Exception {
        Mockito.when(facultyService.findByNameIgnoreCase("гриффиндор")).thenReturn(List.of(testFaculty));

        mockMvc.perform(get("/faculty/name/гриффиндор"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Гриффиндор"));
    }

    @Test
    void testGetFacultiesByNameIgnoreCaseNotFound() throws Exception {
        Mockito.when(facultyService.findByNameIgnoreCase("несуществующий")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/faculty/name/несуществующий"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFacultiesByColorIgnoreCase() throws Exception {
        Mockito.when(facultyService.findByColorIgnoreCase("красный")).thenReturn(List.of(testFaculty));

        mockMvc.perform(get("/faculty/color-ignore-case/красный"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].color").value("Красный"));
    }

    @Test
    void testGetFacultiesByColorIgnoreCaseNotFound() throws Exception {
        Mockito.when(facultyService.findByColorIgnoreCase("несуществующий")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/faculty/color-ignore-case/несуществующий"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchFaculties() throws Exception {
        Mockito.when(facultyService.findByNameOrColorIgnoreCase("гриф")).thenReturn(List.of(testFaculty));

        mockMvc.perform(get("/faculty/search")
                        .param("nameOrColor", "гриф"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Гриффиндор"));
    }

    @Test
    void testSearchFacultiesNotFound() throws Exception {
        Mockito.when(facultyService.findByNameOrColorIgnoreCase("несуществующий")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/faculty/search")
                        .param("nameOrColor", "несуществующий"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFacultyWithStudents() throws Exception {
        Mockito.when(facultyService.facultyWithStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/faculty/faculty-students"))
                .andExpect(status().isOk());
    }
}
