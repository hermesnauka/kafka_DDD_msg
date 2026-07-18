package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.DisplayName;
import com.kafkaddd.chat.identity.domain.Email;
import com.kafkaddd.chat.identity.domain.PasswordHasher;
import com.kafkaddd.chat.identity.domain.Timestamp;
import com.kafkaddd.chat.identity.domain.User;
import com.kafkaddd.chat.identity.domain.UserId;
import com.kafkaddd.chat.identity.domain.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-1: user registration. */
@Service
public class RegisterUserUseCase {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;

  public RegisterUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
  }

  @Transactional
  public UserView register(String rawEmail, String rawPassword, String rawDisplayName) {
    Email email = new Email(rawEmail);
    if (userRepository.existsByEmail(email)) {
      throw new EmailAlreadyInUseException();
    }
    User user =
        User.register(
            UserId.newId(), email, rawPassword, new DisplayName(rawDisplayName), passwordHasher, Timestamp.now());
    try {
      userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      // The existsByEmail check above can't prevent a race between two
      // concurrent registrations for the same email; the DB's unique
      // constraint (V1__create_users_table.sql) is the real guard, this
      // just translates its violation into a clean API-facing error.
      throw new EmailAlreadyInUseException();
    }
    return UserView.from(user);
  }
}
