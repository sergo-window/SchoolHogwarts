package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.service.StudentService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerParallelUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @Test
    void testPrintParallelCallsCorrectMethods() throws Exception {

        doNothing().when(studentService).printStudentsInParallel();

        mockMvc.perform(get("/student/print-parallel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("started"));

        verify(studentService, times(1)).printStudentsInParallel();
    }
}