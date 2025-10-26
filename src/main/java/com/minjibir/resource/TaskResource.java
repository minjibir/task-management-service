package com.minjibir.resource;

import com.minjibir.dto.TaskRequest;
import com.minjibir.dto.TaskResponse;
import com.minjibir.exception.DuplicateTaskException;
import com.minjibir.repository.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
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
         .orElseThrow(() -> new NotFoundException("Task with the id does not exist"));
   }

   @DELETE
   @Path("/{id}")
   @Transactional
   public Response deleteTask(UUID id) {
      return taskRepository
         .findByIdOptional(id)
         .map(t -> {
            taskRepository.deleteById(t.id);
            return Response.noContent().build();
         })
         .orElseThrow(() -> new NotFoundException("Task with the id does not exist"));
   }


   @POST
   @Transactional
   public Response createTask(@NotNull(message = "Request body cannot be empty") @Valid TaskRequest request) {
      if (taskRepository.findByTitle(request.title()).isPresent())
         throw new DuplicateTaskException("Task with the same title already exists");

      var task = request.toTask();

      taskRepository.persist(task);

      return Response
         .created(URI.create("/api/tasks/" + task.id))
         .entity(TaskResponse.fromTask(task))
         .build();
   }

   @PUT
   @Path("/{id}")
   @Transactional
   public Response updateTask(@PathParam("id") UUID id, @NotNull(message = "Request body cannot be empty") @Valid TaskRequest request) {
      return taskRepository
         .findByIdOptional(id)
         .map(t -> {
            taskRepository
               .findByTitle(request.title())
               .ifPresent(existing -> {
                  if (!existing.id.equals(t.id))
                     throw new DuplicateTaskException("Task with the same title already exists");
               });

            t.title = request.title();
            request.description().ifPresent(v -> t.description = v);

            return Response.ok(TaskResponse.fromTask(t)).build();
         })
         .orElseThrow(() -> new NotFoundException("Task not found"));
   }

}
