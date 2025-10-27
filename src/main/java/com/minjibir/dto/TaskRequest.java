package com.minjibir.dto;

import com.minjibir.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Optional;

public record TaskRequest(

   @NotBlank(message = "Title must not be empty")
   @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
   String title,
   Optional<String> description
) {
   public Task toTask() {
      return new Task(
         title,
         description.orElse(null)
      );
   }
}
