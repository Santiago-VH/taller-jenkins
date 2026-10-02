package com.icesi.studytrack.service;

import com.icesi.studytrack.exception.TaskNotFoundException;
import com.icesi.studytrack.model.Task;
import com.icesi.studytrack.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task create(Task task) {
        task.setId(null);
        task.setCompleted(false);
        return taskRepository.save(task);
    }

    public Task update(Long id, Task updated) {
        Task existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setSubject(updated.getSubject());
        existing.setDueDate(updated.getDueDate());
        return taskRepository.save(existing);
    }

    public Task markAsCompleted(Long id) {
        Task existing = findById(id);
        existing.setCompleted(true);
        return taskRepository.save(existing);
    }

    public void delete(Long id) {
        Task existing = findById(id);
        taskRepository.delete(existing);
    }
}
