package com.kafkaddd.chat.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * Domain-layer tests: no Spring context, no BCrypt, no database — just the
 * aggregate's own invariants (NFR-Arch-1), using a deterministic fake
 * {@link PasswordHasher} test double instead of the real BCrypt adapter.
 */
class UserTest {

  private static final Timestamp NOW = Timestamp.now();

  /** Deterministic stand-in for the real BCrypt-backed adapter (identity.infrastructure). */
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

  private static User registerAlice() {
    return User.register(
        UserId.newId(), new Email("Alice@Example.com"), "correct-horse", new DisplayName("Alice"), FAKE_HASHER, NOW);
  }

  @Test
  void authenticatingWithTheCorrectPasswordSucceeds() {
    User user = registerAlice();
    assertThat(user.authenticate("correct-horse", FAKE_HASHER, NOW)).isTrue();
    assertThat(user.failedLoginAttempts()).isZero();
  }

  @Test
  void authenticatingWithTheWrongPasswordFailsAndCountsTheAttempt() {
    User user = registerAlice();
    assertThat(user.authenticate("wrong-password", FAKE_HASHER, NOW)).isFalse();
    assertThat(user.failedLoginAttempts()).isEqualTo(1);
  }

  @Test
  void aSuccessfulLoginResetsThePriorFailureCount() {
    User user = registerAlice();
    user.authenticate("wrong", FAKE_HASHER, NOW);
    user.authenticate("wrong-again", FAKE_HASHER, NOW);
    assertThat(user.failedLoginAttempts()).isEqualTo(2);

    user.authenticate("correct-horse", FAKE_HASHER, NOW);
    assertThat(user.failedLoginAttempts()).isZero();
  }

  @Test
  void accountLocksAfterTheMaxFailedAttemptsAndRejectsEvenACorrectPasswordUntilItExpires() {
    User user = registerAlice();
    for (int i = 0; i < User.MAX_FAILED_ATTEMPTS; i++) {
      user.authenticate("wrong", FAKE_HASHER, NOW);
    }
    assertThat(user.isLocked(NOW)).isTrue();

    assertThatThrownBy(() -> user.authenticate("correct-horse", FAKE_HASHER, NOW))
        .isInstanceOf(AccountLockedException.class);

    Timestamp afterLockout = NOW.plus(Duration.ofMinutes(User.LOCKOUT_MINUTES + 1));
    assertThat(user.isLocked(afterLockout)).isFalse();
    assertThat(user.authenticate("correct-horse", FAKE_HASHER, afterLockout)).isTrue();
  }

  @Test
  void changingThePasswordInvalidatesTheOldOne() {
    User user = registerAlice();
    user.changePassword("new-password", FAKE_HASHER);

    assertThat(user.authenticate("correct-horse", FAKE_HASHER, NOW)).isFalse();
    assertThat(user.authenticate("new-password", FAKE_HASHER, NOW)).isTrue();
  }

  @Test
  void changingTheDisplayNameUpdatesIt() {
    User user = registerAlice();
    user.changeDisplayName(new DisplayName("Alice Cooper"));
    assertThat(user.displayName()).isEqualTo(new DisplayName("Alice Cooper"));
  }

  @Test
  void emailIsNormalizedToLowercaseAndTrimmed() {
    assertThat(new Email("  Alice@Example.COM  ").value()).isEqualTo("alice@example.com");
  }

  @Test
  void emailRejectsInvalidFormat() {
    assertThatThrownBy(() -> new Email("not-an-email")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void displayNameRejectsBlankAndOversizedText() {
    assertThatThrownBy(() -> new DisplayName(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DisplayName("x".repeat(DisplayName.MAX_LENGTH + 1)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void hashedPasswordNeverAppearsInToString() {
    HashedPassword hashed = new HashedPassword("super-secret-hash-value");
    assertThat(hashed.toString()).doesNotContain("super-secret-hash-value");
  }
}
