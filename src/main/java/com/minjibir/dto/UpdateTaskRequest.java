package com.minjibir.dto;

import com.minjibir.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record UpdateTaskRequest(
   @NotBlank String title,
   Optional<String> description,
   Optional<TaskStatus> status
) {
}