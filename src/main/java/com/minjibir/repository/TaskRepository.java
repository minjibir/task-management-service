package com.minjibir.repository;

import com.minjibir.model.Task;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TaskRepository implements PanacheRepositoryBase<Task, UUID> {
   public Optional<Task> findByTitle(String title) {
      return find("title", title).singleResultOptional();
   }
}
