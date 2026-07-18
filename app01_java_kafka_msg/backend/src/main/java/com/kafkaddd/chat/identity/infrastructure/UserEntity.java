package com.kafkaddd.chat.identity.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence model for {@link com.kafkaddd.chat.identity.domain.User}.
 * Deliberately a separate class from the domain aggregate — the domain
 * stays framework-free (NFR-Arch-1); {@link UserRepositoryAdapter} maps
 * between the two.
 */
@Entity
@Table(name = "users")
class UserEntity {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String hashedPassword;

  @Column(nullable = false)
  private String displayName;

  @Column(nullable = false)
  private Instant registeredAt;

  @Column(nullable = false)
  private int failedLoginAttempts;

  private Instant lockedUntil;

  protected UserEntity() {
    // required by JPA
  }

  UserEntity(
      UUID id,
      String email,
      String hashedPassword,
      String displayName,
      Instant registeredAt,
      int failedLoginAttempts,
      Instant lockedUntil) {
    this.id = id;
    this.email = email;
    this.hashedPassword = hashedPassword;
    this.displayName = displayName;
    this.registeredAt = registeredAt;
    this.failedLoginAttempts = failedLoginAttempts;
    this.lockedUntil = lockedUntil;
  }

  UUID getId() {
    return id;
  }

  String getEmail() {
    return email;
  }

  String getHashedPassword() {
    return hashedPassword;
  }

  String getDisplayName() {
    return displayName;
  }

  Instant getRegisteredAt() {
    return registeredAt;
  }

  int getFailedLoginAttempts() {
    return failedLoginAttempts;
  }

  Instant getLockedUntil() {
    return lockedUntil;
  }
}
