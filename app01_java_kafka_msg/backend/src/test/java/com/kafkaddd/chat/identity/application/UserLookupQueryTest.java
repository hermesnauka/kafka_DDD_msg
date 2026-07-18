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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserLookupQueryTest {

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

  @Mock private UserRepository userRepository;

  @Test
  void returnsAViewOfTheUserWhenTheEmailIsRegistered() {
    User bob =
        User.register(
            UserId.newId(), new Email("bob@example.com"), "secret", new DisplayName("Bob"), FAKE_HASHER, Timestamp.now());
    when(userRepository.findByEmail(new Email("bob@example.com"))).thenReturn(Optional.of(bob));

    UserView view = new UserLookupQuery(userRepository).getByEmail("bob@example.com");

    assertThat(view.displayName()).isEqualTo("Bob");
  }

  @Test
  void throwsWhenNoUserHasThatEmail() {
    when(userRepository.findByEmail(new Email("ghost@example.com"))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> new UserLookupQuery(userRepository).getByEmail("ghost@example.com"))
        .isInstanceOf(NoSuchUserException.class);
  }
}
