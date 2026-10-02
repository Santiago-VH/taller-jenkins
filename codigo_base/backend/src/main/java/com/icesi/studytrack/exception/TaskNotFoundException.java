package com.icesi.studytrack.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("No se encontró la tarea con id " + id);
    }
}
