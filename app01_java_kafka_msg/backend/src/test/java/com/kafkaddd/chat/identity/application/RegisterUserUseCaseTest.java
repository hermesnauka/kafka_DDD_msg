package com.kafkaddd.chat.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kafkaddd.chat.identity.domain.Email;
import com.kafkaddd.chat.identity.domain.HashedPassword;
import com.kafkaddd.chat.identity.domain.PasswordHasher;
import com.kafkaddd.chat.identity.domain.User;
import com.kafkaddd.chat.identity.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordHasher passwordHasher;

  private RegisterUserUseCase useCase() {
    return new RegisterUserUseCase(userRepository, passwordHasher);
  }

  @Test
  void registeringANewEmailSavesTheUserAndReturnsAView() {
    when(passwordHasher.hash(any())).thenReturn(new HashedPassword("hashed"));

    UserView view = useCase().register("alice@example.com", "secret123", "Alice");

    assertThat(view.email()).isEqualTo("alice@example.com");
    assertThat(view.displayName()).isEqualTo("Alice");
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void registeringAnAlreadyRegisteredEmailIsRejectedWithoutSaving() {
    when(userRepository.existsByEmail(new Email("alice@example.com"))).thenReturn(true);

    assertThatThrownBy(() -> useCase().register("alice@example.com", "secret123", "Alice"))
        .isInstanceOf(EmailAlreadyInUseException.class);
    verify(userRepository, never()).save(any());
  }

  @Test
  void aRaceOnTheUniqueEmailConstraintIsTranslatedToTheSameCleanException() {
    when(passwordHasher.hash(any())).thenReturn(new HashedPassword("hashed"));
    when(userRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate key"));

    assertThatThrownBy(() -> useCase().register("alice@example.com", "secret123", "Alice"))
        .isInstanceOf(EmailAlreadyInUseException.class);
  }
}
