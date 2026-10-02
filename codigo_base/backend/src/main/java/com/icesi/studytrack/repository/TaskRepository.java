package com.icesi.studytrack.repository;

import com.icesi.studytrack.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
