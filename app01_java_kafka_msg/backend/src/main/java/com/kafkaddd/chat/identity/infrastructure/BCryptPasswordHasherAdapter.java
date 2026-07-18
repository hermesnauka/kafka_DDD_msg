package com.kafkaddd.chat.identity.infrastructure;

import com.kafkaddd.chat.identity.domain.HashedPassword;
import com.kafkaddd.chat.identity.domain.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** BCrypt-backed adapter for the domain's {@link PasswordHasher} port (SR-1: cost &ge; 12). */
@Component
class BCryptPasswordHasherAdapter implements PasswordHasher {

  private static final int COST_FACTOR = 12;

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(COST_FACTOR);

  @Override
  public HashedPassword hash(String rawPassword) {
    return new HashedPassword(encoder.encode(rawPassword));
  }

  @Override
  public boolean matches(String rawPassword, HashedPassword hashed) {
    return encoder.matches(rawPassword, hashed.value());
  }
}
