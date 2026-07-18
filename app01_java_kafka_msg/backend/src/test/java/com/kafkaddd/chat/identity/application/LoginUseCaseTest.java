package com.kafkaddd.chat.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kafkaddd.chat.identity.domain.AccountLockedException;
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
class LoginUseCaseTest {

  private static final Timestamp NOW = Timestamp.now();

  // A real (non-mocked) fake hasher, same style as UserTest — User is a
  // concrete final domain class, not mocked; only the port interfaces are.
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
  @Mock private AccessTokenIssuer accessTokenIssuer;
  @Mock private RefreshTokenStore refreshTokenStore;

  private LoginUseCase useCase() {
    return new LoginUseCase(userRepository, FAKE_HASHER, accessTokenIssuer, refreshTokenStore);
  }

  private User registerAlice() {
    return User.register(
        UserId.newId(), new Email("alice@example.com"), "correct-horse", new DisplayName("Alice"), FAKE_HASHER, NOW);
  }

  @Test
  void unknownEmailIsRejectedWithoutTouchingTokenIssuers() {
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase().login("nobody@example.com", "whatever"))
        .isInstanceOf(InvalidCredentialsException.class);
    verifyNoInteractions(accessTokenIssuer, refreshTokenStore);
    verify(userRepository, never()).save(any());
  }

  @Test
  void wrongPasswordIsRejectedButTheFailedAttemptIsStillPersisted() {
    User alice = registerAlice();
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(alice));

    assertThatThrownBy(() -> useCase().login("alice@example.com", "wrong-password"))
        .isInstanceOf(InvalidCredentialsException.class);

    verify(userRepository, times(1)).save(alice);
    assertThat(alice.failedLoginAttempts()).isEqualTo(1);
    verifyNoInteractions(accessTokenIssuer, refreshTokenStore);
  }

  @Test
  void correctPasswordIssuesAFreshTokenPairAndPersistsTheResetAttemptCount() {
    User alice = registerAlice();
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(alice));
    when(accessTokenIssuer.issue(alice.id())).thenReturn("access-token");
    when(refreshTokenStore.issue(alice.id())).thenReturn("refresh-token");

    AuthResult result = useCase().login("alice@example.com", "correct-horse");

    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isEqualTo("refresh-token");
    assertThat(result.userId()).isEqualTo(alice.id());
    verify(userRepository, times(1)).save(alice);
  }

  @Test
  void aLockedAccountRejectsEvenTheCorrectPasswordAndStillPersistsState() {
    User alice = registerAlice();
    for (int i = 0; i < User.MAX_FAILED_ATTEMPTS; i++) {
      alice.authenticate("wrong", FAKE_HASHER, NOW);
    }
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(alice));

    assertThatThrownBy(() -> useCase().login("alice@example.com", "correct-horse"))
        .isInstanceOf(AccountLockedException.class);
    verify(userRepository, times(1)).save(alice);
    verifyNoInteractions(accessTokenIssuer, refreshTokenStore);
  }
}
