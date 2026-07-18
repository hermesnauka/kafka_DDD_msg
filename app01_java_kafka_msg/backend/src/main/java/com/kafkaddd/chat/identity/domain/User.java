package com.kafkaddd.chat.identity.domain;

import java.time.Duration;
import java.util.Objects;

/**
 * Aggregate root for a registered user (FR-1). Encodes SR-1's throttling
 * policy directly: after {@value #MAX_FAILED_ATTEMPTS} consecutive failed
 * login attempts, the account locks for {@value #LOCKOUT_MINUTES} minutes.
 * The actual password hashing algorithm is injected via {@link PasswordHasher}
 * so this class stays framework-free (NFR-Arch-1) — BCrypt specifics live
 * in the {@code identity.infrastructure} adapter.
 */
public final class User {

  public static final int MAX_FAILED_ATTEMPTS = 5;
  public static final long LOCKOUT_MINUTES = 15;

  private final UserId id;
  private final Email email;
  private final Timestamp registeredAt;
  private HashedPassword hashedPassword;
  private DisplayName displayName;
  private int failedLoginAttempts;
  private Timestamp lockedUntil;

  private User(UserId id, Email email, HashedPassword hashedPassword, DisplayName displayName, Timestamp registeredAt) {
    this.id = Objects.requireNonNull(id, "id");
    this.email = Objects.requireNonNull(email, "email");
    this.hashedPassword = Objects.requireNonNull(hashedPassword, "hashedPassword");
    this.displayName = Objects.requireNonNull(displayName, "displayName");
    this.registeredAt = Objects.requireNonNull(registeredAt, "registeredAt");
  }

  /**
   * Registers a new user. Email uniqueness is an application-layer
   * concern (requires a repository lookup) — see
   * {@code identity.application}'s registration use case.
   */
  public static User register(
      UserId id, Email email, String rawPassword, DisplayName displayName, PasswordHasher hasher, Timestamp now) {
    return new User(id, email, hasher.hash(rawPassword), displayName, now);
  }

  /**
   * Rebuilds a {@code User} from already-persisted state — used only by the
   * repository adapter in {@code identity.infrastructure}. Unlike
   * {@link #register}, this never hashes anything: {@code hashedPassword}
   * must already be a hash, and {@code failedLoginAttempts}/{@code lockedUntil}
   * are restored as-is rather than recomputed.
   */
  public static User reconstitute(
      UserId id,
      Email email,
      HashedPassword hashedPassword,
      DisplayName displayName,
      Timestamp registeredAt,
      int failedLoginAttempts,
      Timestamp lockedUntil) {
    User user = new User(id, email, hashedPassword, displayName, registeredAt);
    user.failedLoginAttempts = failedLoginAttempts;
    user.lockedUntil = lockedUntil;
    return user;
  }

  /**
   * Attempts to authenticate with {@code rawPassword}. Throws
   * {@link AccountLockedException} if the account is currently locked out;
   * otherwise records the attempt's outcome (resetting the failure count on
   * success, incrementing — and possibly locking — on failure) and returns
   * whether it succeeded.
   */
  public boolean authenticate(String rawPassword, PasswordHasher hasher, Timestamp now) {
    if (isLocked(now)) {
      throw new AccountLockedException(id, lockedUntil);
    }
    boolean matches = hasher.matches(rawPassword, hashedPassword);
    if (matches) {
      recordSuccessfulLogin();
    } else {
      recordFailedLogin(now);
    }
    return matches;
  }

  public void changePassword(String newRawPassword, PasswordHasher hasher) {
    this.hashedPassword = hasher.hash(newRawPassword);
  }

  public void changeDisplayName(DisplayName newDisplayName) {
    this.displayName = Objects.requireNonNull(newDisplayName, "newDisplayName");
  }

  public boolean isLocked(Timestamp now) {
    return lockedUntil != null && now.isBefore(lockedUntil);
  }

  private void recordSuccessfulLogin() {
    failedLoginAttempts = 0;
    lockedUntil = null;
  }

  private void recordFailedLogin(Timestamp now) {
    failedLoginAttempts++;
    if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
      lockedUntil = now.plus(Duration.ofMinutes(LOCKOUT_MINUTES));
    }
  }

  public UserId id() {
    return id;
  }

  public Email email() {
    return email;
  }

  public DisplayName displayName() {
    return displayName;
  }

  public HashedPassword hashedPassword() {
    return hashedPassword;
  }

  public Timestamp registeredAt() {
    return registeredAt;
  }

  public int failedLoginAttempts() {
    return failedLoginAttempts;
  }

  public Timestamp lockedUntil() {
    return lockedUntil;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
