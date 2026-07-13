package ru.tm.task.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.tm.task.entity.Task;
import ru.tm.task.entity.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
