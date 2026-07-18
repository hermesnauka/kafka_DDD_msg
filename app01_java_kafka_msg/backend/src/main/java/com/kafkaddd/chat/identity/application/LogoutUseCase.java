package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.UserId;
import org.springframework.stereotype.Service;

/** Revokes every refresh token issued to a user, so a stolen-but-unused one can't be replayed after logout. */
@Service
public class LogoutUseCase {

  private final RefreshTokenStore refreshTokenStore;

  public LogoutUseCase(RefreshTokenStore refreshTokenStore) {
    this.refreshTokenStore = refreshTokenStore;
  }

  public void logout(UserId userId) {
    refreshTokenStore.revokeAll(userId);
  }
}
