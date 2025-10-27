package com.minjibir.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
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

   @CreationTimestamp
   @Column(name = "created_at", nullable = false, updatable = false)
   public LocalDateTime createdAt;

   @UpdateTimestamp
   @Column(name = "updated_at", nullable = false)
   public LocalDateTime updatedAt;

   public Task() {
   }

   public Task(String title, String description) {
      this.title = title;
      this.description = description;
      this.status = TaskStatus.PENDING;
   }
}
