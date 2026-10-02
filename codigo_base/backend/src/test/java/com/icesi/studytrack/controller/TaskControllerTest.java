package com.icesi.studytrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icesi.studytrack.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndRetrieveTask_endToEnd() throws Exception {
        Task task = new Task("Preparar sustentación", "Ingeniería de Software V", LocalDate.now().plusDays(2));

        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Preparar sustentación")))
                .andExpect(jsonPath("$.completed", is(false)))
                .andReturn().getResponse().getContentAsString();

        Task created = objectMapper.readValue(response, Task.class);

        mockMvc.perform(get("/api/tasks/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Preparar sustentación")));
    }

    @Test
    void getAllTasks_returnsOkAndList() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void completeTask_marksAsCompleted() throws Exception {
        Task task = new Task("Revisar Jenkinsfile", "DevOps", LocalDate.now().plusDays(1));

        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andReturn().getResponse().getContentAsString();

        Task created = objectMapper.readValue(response, Task.class);

        mockMvc.perform(patch("/api/tasks/" + created.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void getNonExistentTask_returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_removesIt() throws Exception {
        Task task = new Task("Tarea a borrar", "DevOps", LocalDate.now());

        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andReturn().getResponse().getContentAsString();

        Task created = objectMapper.readValue(response, Task.class);

        mockMvc.perform(delete("/api/tasks/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + created.getId()))
                .andExpect(status().isNotFound());
    }
}
