package com.minjibir.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@DynamicUpdate
public class Task extends PanacheEntityBase {
   @Id
   @Column(columnDefinition = "uuid", nullable = false, updatable = false)
   @GeneratedValue(strategy = GenerationType.UUID)
   public UUID id;

   @Column(nullable = false, unique = true)
   public String title;

   public String description;

   @Column(nullable = false)
   @Enumerated(EnumType.STRING)
   public TaskStatus status;

   @Column(nullable = false, updatable = false)
   public LocalDateTime createdAt;

   @Column(nullable = false)
   public LocalDateTime updatedAt;

   public Task() {
   }

   public Task(String title, String description) {
      this.title = title;
      this.description = description;
      this.status = TaskStatus.PENDING;
      this.createdAt = LocalDateTime.now();
      this.updatedAt = LocalDateTime.now();
   }

   @PreUpdate
   public void preUpdate() {
      this.updatedAt = LocalDateTime.now();
   }
}
