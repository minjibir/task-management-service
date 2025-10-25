package com.minjibir.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minjibir.dto.TaskRequest;
import com.minjibir.dto.UpdateTaskRequest;
import com.minjibir.model.Task;
import com.minjibir.model.TaskStatus;
import com.minjibir.repository.TaskRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class TaskResourceTest {

   public static final String PATH = "/api/tasks";

   @Inject
   ObjectMapper objectMapper;

   @Inject
   TaskRepository taskRepository;

   @Inject
   UserTransaction userTransaction;

   List<Task> tasks = List.of(
      new Task(
         "TDD with Quarkus",
         "Get familiar with Quarkus"
      ),
      new Task(
         "Enterprise Quarkus",
         null
      )
   );

   @BeforeEach
   @Transactional
   void setup() {
      taskRepository.deleteAll();
   }

   @Test
   void getAllTasks_shouldReturnEmptyWhenNoTaskIsAvailable() {
      given()
         .when()
         .get(PATH)
         .then()
         .statusCode(200)
         .contentType(ContentType.JSON)
         .body(is("[]"));
   }

   @Test
   void getAllTasks_shouldReturnAllTaskWhenTheyExists() throws Exception {
      userTransaction.begin();
      taskRepository.persist(tasks);
      userTransaction.commit();

      given()
         .when()
         .get(PATH)
         .then()
         .statusCode(200)
         .contentType(ContentType.JSON)
         .body("size()", is(2))
         .body("[0].title", is(tasks.getFirst().title))
         .body("[0].description", is(tasks.getFirst().description))
         .body("[0].status", is(tasks.getFirst().status.toString()))
         .body("[1].title", is(tasks.get(1).title))
         .body("[1].description", is(tasks.get(1).description))
         .body("[1].status", is(tasks.get(1).status.toString()));
   }

   @Test
   void getTaskById_shouldReturnNotFoundWhenNoTaskWithTheSuppliedIdExists() {
      given()
         .when()
         .get(PATH + "/{id}", UUID.randomUUID())
         .then()
         .statusCode(404)
         .body(is(""));
   }

   @Test
   void getTaskById_shouldReturnTheTaskWhenItExists() throws Exception {
      userTransaction.begin();
      taskRepository.persist(tasks.getFirst());
      userTransaction.commit();

      var task = tasks.getFirst();

      given()
         .when()
         .get(PATH + "/{id}", task.id.toString())
         .then()
         .statusCode(200)
         .contentType(ContentType.JSON)
         .body("id", is(task.id.toString()))
         .body("title", is(task.title))
         .body("description", is(task.description))
         .body("status", is(task.status.toString()));
   }

   @Test
   void deleteTask_shouldReturnNotFoundIfTheTaskWithTheSuppliedIdDoesNotExists() {
      var nonExistingId = UUID.randomUUID();

      given()
         .when()
         .delete(PATH + "/{id}", nonExistingId)
         .then()
         .statusCode(404)
         .body(is(""));
   }

   @Test
   void deleteTask_shouldReturnNoContentIfTheTaskWithTheSuppliedIdExists() throws Exception {

      userTransaction.begin();
      taskRepository.persist(tasks.getFirst());
      userTransaction.commit();

      given()
         .when()
         .delete(PATH + "/{id}", tasks.getFirst().id.toString())
         .then()
         .statusCode(204)
         .body(is(""));
   }

   @Test
   void createTask_shouldReturnBadRequestWhenRequestBodyIsNotValid() {
      given()
         .when()
         .contentType(ContentType.JSON)
         .post(PATH)
         .then()
         .statusCode(400);
   }

   @Test
   void createTask_shouldReturnConflictIfTaskWithTheSameTitleAlreadyExists() throws Exception {
      userTransaction.begin();
      taskRepository.persistAndFlush(tasks.getFirst());
      userTransaction.commit();

      var taskRequest = new TaskRequest(tasks.getFirst().title, "Task Description");

      given()
         .when()
         .body(objectMapper.writeValueAsString(taskRequest))
         .contentType(ContentType.JSON)
         .post(PATH)
         .then()
         .statusCode(409)
         .body(is("{\"message\": \"Task with the same title already exists\"}"));
   }

   @Test
   void createTask_shouldSuccessfullyCreateNewTaskWithOnlyTitle() throws JsonProcessingException {
      var taskRequest = new TaskRequest(tasks.getFirst().title, null);

      given()
         .when()
         .body(objectMapper.writeValueAsString(taskRequest))
         .contentType(ContentType.JSON)
         .post(PATH)
         .then()
         .statusCode(201)
         .body("id", notNullValue())
         .body("createdAt", notNullValue())
         .body("updatedAt", notNullValue())
         .body("status", is(TaskStatus.PENDING.toString()))
         .body("title", is(taskRequest.title()))
         .body("description", is(nullValue()));
   }

   @Test
   void createTask_shouldSuccessfullyCreateNewTaskWithTitleAndDescription() throws JsonProcessingException {
      given()
         .when()
         .body(objectMapper.writeValueAsString(tasks.getFirst()))
         .contentType(ContentType.JSON)
         .post(PATH)
         .then()
         .statusCode(201)
         .body("id", notNullValue())
         .body("createdAt", notNullValue())
         .body("updatedAt", notNullValue())
         .body("status", is(TaskStatus.PENDING.toString()))
         .body("title", is(tasks.getFirst().title))
         .body("description", is(notNullValue()))
         .body("description", is(tasks.getFirst().description));
   }

   @Test
   void updateTask_shouldReturnBadRequestWhenRequestBodyIsNotValid() {
      given()
         .when()
         .contentType(ContentType.JSON)
         .put(PATH)
         .then()
         .statusCode(400);
   }

   @Test
   void updateTask_shouldReturnNotFoundWhenTaskIdDoesNotMatchAnyExistingTask() throws JsonProcessingException {
      var nonExistingTask = new UpdateTaskRequest(
         UUID.randomUUID(),
         tasks.getFirst().title,
         Optional.ofNullable(tasks.getFirst().description)
      );

      given()
         .when()
         .contentType(ContentType.JSON)
         .body(objectMapper.writeValueAsString(nonExistingTask))
         .put(PATH)
         .then()
         .statusCode(404)
         .body(is("{\"message\": \"Task not found\"}"));
   }


   @Test
   void updateTask_shouldReturnConflictWhenTaskWithTheSameTitleAlreadyExists() throws Exception {
      userTransaction.begin();
      taskRepository.persist(tasks);
      userTransaction.commit();

      var existingTask = new UpdateTaskRequest(
         tasks.getFirst().id,
         tasks.get(1).title,
         Optional.ofNullable(tasks.getFirst().description)
      );

      given()
         .when()
         .contentType(ContentType.JSON)
         .body(objectMapper.writeValueAsString(existingTask))
         .put(PATH)
         .then()
         .statusCode(409)
         .body(is("{\"message\": \"Task with the same title already exists\"}"));
   }

   @Test
   void updateTask_shouldReturnOkWhenSuccessful() throws Exception {
      userTransaction.begin();
      taskRepository.persist(tasks.getFirst());
      userTransaction.commit();

      var updatedTask = new UpdateTaskRequest(
         tasks.getFirst().id,
         "Updated Task Title",
         Optional.of("Updated Task Description")
      );

      given()
         .when()
         .contentType(ContentType.JSON)
         .body(objectMapper.writeValueAsString(updatedTask))
         .put(PATH)
         .then()
         .statusCode(200)
         .body("id", is(tasks.getFirst().id.toString()))
         .body("title", is(updatedTask.title()))
         .body("description", is(updatedTask.description().orElse(null)))
         .body("updatedAt", not(tasks.getFirst().updatedAt.toString()));
   }

}
