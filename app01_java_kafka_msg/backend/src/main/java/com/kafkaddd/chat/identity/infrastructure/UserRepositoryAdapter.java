package com.kafkaddd.chat.identity.infrastructure;

import com.kafkaddd.chat.identity.domain.DisplayName;
import com.kafkaddd.chat.identity.domain.Email;
import com.kafkaddd.chat.identity.domain.HashedPassword;
import com.kafkaddd.chat.identity.domain.Timestamp;
import com.kafkaddd.chat.identity.domain.User;
import com.kafkaddd.chat.identity.domain.UserId;
import com.kafkaddd.chat.identity.domain.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adapter implementing the domain's {@link UserRepository} port with Spring
 * Data JPA. This is the only class allowed to know both the domain model
 * and {@link UserEntity} — everything else in the application only ever
 * sees {@link User}.
 */
@Repository
class UserRepositoryAdapter implements UserRepository {

  private final UserJpaRepository jpaRepository;

  UserRepositoryAdapter(UserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<User> findById(UserId id) {
    return jpaRepository.findById(id.value()).map(this::toDomain);
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return jpaRepository.findByEmail(email.value()).map(this::toDomain);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return jpaRepository.existsByEmail(email.value());
  }

  @Override
  public User save(User user) {
    jpaRepository.save(toEntity(user));
    return user;
  }

  private User toDomain(UserEntity entity) {
    Timestamp lockedUntil =
        entity.getLockedUntil() == null ? null : new Timestamp(entity.getLockedUntil());
    return User.reconstitute(
        new UserId(entity.getId()),
        new Email(entity.getEmail()),
        new HashedPassword(entity.getHashedPassword()),
        new DisplayName(entity.getDisplayName()),
        new Timestamp(entity.getRegisteredAt()),
        entity.getFailedLoginAttempts(),
        lockedUntil);
  }

  private UserEntity toEntity(User user) {
    return new UserEntity(
        user.id().value(),
        user.email().value(),
        user.hashedPassword().value(),
        user.displayName().value(),
        user.registeredAt().value(),
        user.failedLoginAttempts(),
        user.lockedUntil() == null ? null : user.lockedUntil().value());
  }
}
