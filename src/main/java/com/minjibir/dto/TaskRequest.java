package com.minjibir.dto;

import com.minjibir.model.Task;

public record TaskRequest(
   String title,
   String description
) {
   public Task toTask() {
      return new Task(
         title,
         description
      );
   }
}
