package com.Gaurishankar.register.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Token {

  @Id
  @GeneratedValue
  private Integer id;

  private String token;
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;
  private LocalDateTime validatedAt;

  @ManyToOne
  @JoinColumn(name = "userId", nullable = false)
  private ApplicationUser user;

}
