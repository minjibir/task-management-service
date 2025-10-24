package com.minjibir.resource;

import com.minjibir.dto.TaskResponse;
import com.minjibir.repository.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Optional;

@Path("/api/tasks")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TaskResource {

   @Inject
   TaskRepository taskRepository;

   @GET
   public List<TaskResponse> getAllTasks() {
      return taskRepository
         .findAll()
         .list()
         .stream()
         .map(task -> new TaskResponse(
            task.id,
            task.title,
            Optional.ofNullable(task.description),
            task.status,
            task.createdAt,
            task.updatedAt
         )).toList();
   }
}
