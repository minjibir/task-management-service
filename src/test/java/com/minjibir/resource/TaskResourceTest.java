package com.minjibir.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minjibir.dto.TaskRequest;
import com.minjibir.model.Task;
import com.minjibir.model.TaskStatus;
import com.minjibir.repository.TaskRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
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
         .body("status", is("ERROR"))
         .body("message", is("Task with the id does not exist"));
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
         .body("status", is("ERROR"))
         .body("message", is("Task with the id does not exist"));
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
   void createTask_shouldReturnBadRequestWhenRequestBodyIsEmpty() {
      given()
         .when()
         .contentType(ContentType.JSON)
         .post(PATH)
         .then()
         .statusCode(400)
         .body("status", is("ERROR"))
         .body("message", is("Invalid request body"))
         .body("data[0]", is("Request body cannot be empty"));
   }

   @Test
   void createTask_shouldReturnBadRequestWhenTitleIsNotSupplied() {
      given()
         .when()
         .contentType(ContentType.JSON)
         .body("{\"description\": \"Task Description\"}")
         .post(PATH)
         .then()
         .statusCode(400)
         .body("status", is("ERROR"))
         .body("message", is("Invalid request body"))
         .body("data[0]", is("Title must not be empty"));
   }

   @Test
   void createTask_shouldReturnBadRequestWhenTitleIsLessThan3Characters() {
      given()
         .when()
         .contentType(ContentType.JSON)
         .body("{\"title\": \"ab\", \"description\": \"Task Description\"}")
         .post(PATH)
         .then()
         .statusCode(400)
         .body("status", is("ERROR"))
         .body("message", is("Invalid request body"))
         .body("data[0]", is("Title must be between 3 and 200 characters"));
   }

   @Test
   void createTask_shouldReturnBadRequestWhenTitleIsMoreThan200Characters() {

      given()
         .when()
         .contentType(ContentType.JSON)
         .body("{\"title\": \"" + "a".repeat(201) + "\", \"description\": \"Task Description\"}")
         .post(PATH)
         .then()
         .statusCode(400)
         .body("status", is("ERROR"))
         .body("message", is("Invalid request body"))
         .body("data[0]", is("Title must be between 3 and 200 characters"));
   }

   @Test
   void createTask_shouldReturnConflictIfTaskWithTheSameTitleAlreadyExists() throws Exception {
      userTransaction.begin();
      taskRepository.persistAndFlush(tasks.getFirst());
      userTransaction.commit();

      var taskRequest = new TaskRequest(tasks.getFirst().title, Optional.of("Task Description"));

      given()
         .when()
         .body(objectMapper.writeValueAsString(taskRequest))
         .contentType(ContentType.JSON)
         .post(PATH)
         .then()
         .statusCode(409)
         .body("status", is("ERROR"))
         .body("message", is("Task with the same title already exists"));
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
   void updateTask_shouldReturnNotFoundWhenTaskIdDoesNotMatchAnyExistingTask() throws JsonProcessingException {
      var nonExistingTask = new TaskRequest(
         tasks.getFirst().title,
         Optional.ofNullable(tasks.getFirst().description)
      );

      given()
         .when()
         .contentType(ContentType.JSON)
         .body(objectMapper.writeValueAsString(nonExistingTask))
         .put(PATH + "/{id}", UUID.randomUUID())
         .then()
         .statusCode(404)
         .body("status", is("ERROR"))
         .body("message", is("Task not found"));
   }

   @Test
   void updateTask_shouldReturnConflictWhenTaskWithTheSameTitleAlreadyExists() throws Exception {
      userTransaction.begin();
      taskRepository.persist(tasks);
      userTransaction.commit();

      var existingTask = new TaskRequest(
         tasks.get(1).title,
         Optional.ofNullable(tasks.getFirst().description)
      );

      given()
         .when()
         .contentType(ContentType.JSON)
         .body(objectMapper.writeValueAsString(existingTask))
         .put(PATH + "/{id}", tasks.getFirst().id.toString())
         .then()
         .statusCode(409)
         .body("status", is("ERROR"))
         .body("message", is("Task with the same title already exists"));
   }

   @Test
   void updateTask_shouldReturnOkWhenSuccessful() throws Exception {
      userTransaction.begin();
      taskRepository.persist(tasks.getFirst());
      userTransaction.commit();

      var updatedTask = new TaskRequest(
         "Updated Task Title",
         Optional.of("Updated Task Description")
      );

      given()
         .when()
         .contentType(ContentType.JSON)
         .body(objectMapper.writeValueAsString(updatedTask))
         .put(PATH + "/{id}", tasks.getFirst().id.toString())
         .then()
         .statusCode(200)
         .body("id", is(tasks.getFirst().id.toString()))
         .body("title", is(updatedTask.title()))
         .body("description", is(updatedTask.description().orElse(null)))
         .body("updatedAt", not(tasks.getFirst().updatedAt.toString()));
   }

}
