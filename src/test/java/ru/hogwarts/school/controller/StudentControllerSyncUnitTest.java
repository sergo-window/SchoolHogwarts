package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.service.StudentService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerSyncUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @Test
    void testPrintSynchronizedEndpoint() throws Exception {

        List<String> studentNames = Arrays.asList("Student1", "Student2", "Student3");
        Mockito.when(studentService.getAllStudentNames()).thenReturn(studentNames);
        doNothing().when(studentService).printStudentsSynchronized();

        mockMvc.perform(get("/student/print-synchronized"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("started"))
                .andExpect(jsonPath("$.requirements_met.main_thread_first_two").value(true))
                .andExpect(jsonPath("$.requirements_met.synchronized_method").value(true));
    }
}