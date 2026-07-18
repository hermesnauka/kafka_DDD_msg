package com.kafkaddd.chat.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.kafkaddd.chat.identity.domain.DisplayName;
import com.kafkaddd.chat.identity.domain.Email;
import com.kafkaddd.chat.identity.domain.HashedPassword;
import com.kafkaddd.chat.identity.domain.PasswordHasher;
import com.kafkaddd.chat.identity.domain.Timestamp;
import com.kafkaddd.chat.identity.domain.User;
import com.kafkaddd.chat.identity.domain.UserId;
import com.kafkaddd.chat.identity.domain.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrentUserQueryTest {

  @Mock private UserRepository userRepository;

  private static final PasswordHasher FAKE_HASHER =
      new PasswordHasher() {
        @Override
        public HashedPassword hash(String rawPassword) {
          return new HashedPassword("hashed:" + rawPassword);
        }

        @Override
        public boolean matches(String rawPassword, HashedPassword hashed) {
          return hashed.value().equals("hashed:" + rawPassword);
        }
      };

  @Test
  void returnsAViewOfTheUserWhenFound() {
    User alice =
        User.register(
            UserId.newId(), new Email("alice@example.com"), "secret", new DisplayName("Alice"), FAKE_HASHER, Timestamp.now());
    when(userRepository.findById(alice.id())).thenReturn(Optional.of(alice));

    UserView view = new CurrentUserQuery(userRepository).getById(alice.id());

    assertThat(view.email()).isEqualTo("alice@example.com");
    assertThat(view.displayName()).isEqualTo("Alice");
  }

  @Test
  void throwsWhenTheUserNoLongerExists() {
    UserId ghost = new UserId(UUID.randomUUID());
    when(userRepository.findById(ghost)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> new CurrentUserQuery(userRepository).getById(ghost))
        .isInstanceOf(NoSuchUserException.class);
  }
}
