package com.minjibir.dto;

import com.minjibir.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record TaskResponse(
   UUID id,
   String title,
   Optional<String> description,
   TaskStatus status,
   LocalDateTime createdAt,
   LocalDateTime updatedAt
) {
}
