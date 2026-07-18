package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.Email;
import com.kafkaddd.chat.identity.domain.PasswordHasher;
import com.kafkaddd.chat.identity.domain.Timestamp;
import com.kafkaddd.chat.identity.domain.User;
import com.kafkaddd.chat.identity.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-1/SR-1/SR-2: authenticates a user and issues a fresh access/refresh token pair. */
@Service
public class LoginUseCase {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final AccessTokenIssuer accessTokenIssuer;
  private final RefreshTokenStore refreshTokenStore;

  public LoginUseCase(
      UserRepository userRepository,
      PasswordHasher passwordHasher,
      AccessTokenIssuer accessTokenIssuer,
      RefreshTokenStore refreshTokenStore) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.accessTokenIssuer = accessTokenIssuer;
    this.refreshTokenStore = refreshTokenStore;
  }

  @Transactional
  public AuthResult login(String rawEmail, String rawPassword) {
    User user = userRepository.findByEmail(new Email(rawEmail)).orElseThrow(InvalidCredentialsException::new);

    boolean authenticated;
    try {
      // AccountLockedException is allowed to propagate as-is (a distinct,
      // more informative case than "invalid credentials") — see
      // identity.infrastructure's REST error mapping.
      authenticated = user.authenticate(rawPassword, passwordHasher, Timestamp.now());
    } finally {
      // Persist the attempt's effect on failedLoginAttempts/lockedUntil
      // regardless of outcome. No-op if authenticate() threw before
      // mutating anything (the account was already locked).
      userRepository.save(user);
    }

    if (!authenticated) {
      throw new InvalidCredentialsException();
    }

    String accessToken = accessTokenIssuer.issue(user.id());
    String refreshToken = refreshTokenStore.issue(user.id());
    return new AuthResult(accessToken, refreshToken, user.id());
  }
}
