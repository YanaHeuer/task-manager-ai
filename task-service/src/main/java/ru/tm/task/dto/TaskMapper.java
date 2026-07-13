package ru.tm.task.dto;

import org.mapstruct.Mapper;
import ru.tm.task.entity.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskResponse toResponse(Task task);
}
