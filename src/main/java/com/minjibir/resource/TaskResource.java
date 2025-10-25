package com.minjibir.resource;

import com.minjibir.dto.TaskRequest;
import com.minjibir.dto.TaskResponse;
import com.minjibir.dto.UpdateTaskRequest;
import com.minjibir.model.Task;
import com.minjibir.repository.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("tasks")
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
         .map(TaskResponse::fromTask).toList();
   }

   @GET
   @Path("/{id}")
   public TaskResponse getTaskById(UUID id) {
      return taskRepository
         .findByIdOptional(id)
         .map(TaskResponse::fromTask)
         .orElseThrow(NotFoundException::new);
   }

   @DELETE
   @Path("/{id}")
   @Transactional
   public Response deleteTask(UUID id) {
      Optional<Task> optTask = taskRepository.findByIdOptional(id);

      if (optTask.isPresent()) {
         taskRepository.deleteById(id);
         return Response.noContent().build();
      }

      return Response.status(Response.Status.NOT_FOUND).build();
   }


   @POST
   @Transactional
   public Response createTask(@NotNull TaskRequest request) {
      if (taskRepository.findByTitle(request.title()).isPresent())
         return Response
            .status(Response.Status.CONFLICT)
            .entity("{\"message\": \"Task with the same title already exists\"}")
            .build();

      var task = request.toTask();

      taskRepository.persistAndFlush(task);

      return Response
         .created(URI.create("/api/tasks/" + task.id))
         .entity(TaskResponse.fromTask(task))
         .build();
   }

   @PUT
   @Transactional
   public Response updateTask(@NotNull UpdateTaskRequest request) {
      var task = taskRepository.findById(request.id());

      if (task != null && task.id == request.id()) {
         if (taskRepository.findByTitle(request.title()).isPresent()) {
            return Response
               .status(Response.Status.CONFLICT)
               .entity("{\"message\": \"Task with the same title already exists\"}")
               .build();
         } else {
            task.title = request.title();
            task.description = request.description().orElse(null);
            task.updatedAt = LocalDateTime.now();

            return Response.ok(TaskResponse.fromTask(task)).build();
         }
      }

      return Response
         .status(Response.Status.NOT_FOUND)
         .entity("{\"message\": \"Task not found\"}")
         .build();
   }

}
