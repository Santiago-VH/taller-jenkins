package com.icesi.studytrack.service;

import com.icesi.studytrack.exception.TaskNotFoundException;
import com.icesi.studytrack.model.Task;
import com.icesi.studytrack.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task("Entregar taller CI/CD", "Ingeniería de Software V", LocalDate.now().plusDays(3));
        sampleTask.setId(1L);
    }

    @Test
    void findAll_returnsAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(sampleTask));

        List<Task> result = taskService.findAll();

        assertEquals(1, result.size());
        assertEquals("Entregar taller CI/CD", result.get(0).getTitle());
    }

    @Test
    void findById_whenExists_returnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Task result = taskService.findById(1L);

        assertEquals(sampleTask.getId(), result.getId());
    }

    @Test
    void findById_whenNotExists_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.findById(99L));
    }

    @Test
    void create_setsCompletedFalseAndPersists() {
        Task newTask = new Task("Leer capítulo 3", "DevOps", LocalDate.now().plusDays(1));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.create(newTask);

        assertFalse(result.isCompleted());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void markAsCompleted_setsCompletedTrue() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.markAsCompleted(1L);

        assertTrue(result.isCompleted());
    }

    @Test
    void delete_whenExists_removesTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        taskService.delete(1L);

        verify(taskRepository, times(1)).delete(sampleTask);
    }
}
