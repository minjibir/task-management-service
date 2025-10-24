package com.minjibir.resource;

import com.minjibir.model.Task;
import com.minjibir.repository.TaskRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class TaskResourceTest {

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
         .get("/api/tasks")
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
         .get("/api/tasks")
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
         .get("/api/tasks/{id}", UUID.randomUUID())
         .then()
         .statusCode(404)
         .body(is(""));
   }

   @Test
   void getTaskById_shouldReturnTheTaskWhenItExists() throws Exception {
      userTransaction.begin();
      taskRepository.persist(tasks.getFirst());
      taskRepository.flush();
      userTransaction.commit();

      var task = tasks.getFirst();

      given()
         .when()
         .get("/api/tasks/{id}", task.id.toString())
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
         .delete("/api/tasks/{id}", nonExistingId)
         .then()
         .statusCode(404)
         .body(is(""));
   }

   @Test
   void deleteTask_shouldReturnNoContentIfTheTaskWithTheSuppliedIdExists() throws Exception {

      userTransaction.begin();
      taskRepository.persist(tasks.getFirst());
      taskRepository.flush();
      userTransaction.commit();

      given()
         .when()
         .delete("/api/tasks/{id}", tasks.getFirst().id.toString())
         .then()
         .statusCode(204)
         .body(is(""));
   }
}
