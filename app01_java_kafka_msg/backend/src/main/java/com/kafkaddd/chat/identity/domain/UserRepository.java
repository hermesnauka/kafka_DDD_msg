package com.kafkaddd.chat.identity.domain;

import java.util.Optional;

/**
 * Repository port for {@link User}. The interface is expressed purely in
 * domain types — no Spring/JPA here (NFR-Arch-1). The real adapter
 * (Spring Data JPA-backed) lives in {@code identity.infrastructure}.
 */
public interface UserRepository {

  Optional<User> findById(UserId id);

  Optional<User> findByEmail(Email email);

  boolean existsByEmail(Email email);

  User save(User user);
}
