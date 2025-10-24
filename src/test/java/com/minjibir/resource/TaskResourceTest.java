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
}
