package com.kafkaddd.chat.identity.domain;

/**
 * Port for hashing and verifying passwords (SR-1: "passwords hashed with
 * BCrypt, cost &ge; 12"). The domain depends only on this interface; the
 * real BCrypt-backed implementation is an adapter in
 * {@code identity.infrastructure}, keeping this package framework-free
 * (NFR-Arch-1).
 */
public interface PasswordHasher {

  HashedPassword hash(String rawPassword);

  boolean matches(String rawPassword, HashedPassword hashed);
}
