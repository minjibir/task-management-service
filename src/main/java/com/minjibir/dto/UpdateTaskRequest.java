package com.minjibir.dto;

import java.util.Optional;
import java.util.UUID;

public record UpdateTaskRequest(
   UUID id,
   String title,
   Optional<String> description
) {
}
