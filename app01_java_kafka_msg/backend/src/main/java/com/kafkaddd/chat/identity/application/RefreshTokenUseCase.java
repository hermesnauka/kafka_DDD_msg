package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.UserId;
import org.springframework.stereotype.Service;

/**
 * SR-2's refresh-token rotation: exchanges a valid, not-yet-used refresh
 * token for a brand new access/refresh pair, invalidating the old refresh
 * token in the same step ({@link RefreshTokenStore#consume}).
 */
@Service
public class RefreshTokenUseCase {

  private final AccessTokenIssuer accessTokenIssuer;
  private final RefreshTokenStore refreshTokenStore;

  public RefreshTokenUseCase(AccessTokenIssuer accessTokenIssuer, RefreshTokenStore refreshTokenStore) {
    this.accessTokenIssuer = accessTokenIssuer;
    this.refreshTokenStore = refreshTokenStore;
  }

  public AuthResult refresh(String refreshToken) {
    UserId userId = refreshTokenStore.consume(refreshToken).orElseThrow(InvalidRefreshTokenException::new);
    String newAccessToken = accessTokenIssuer.issue(userId);
    String newRefreshToken = refreshTokenStore.issue(userId);
    return new AuthResult(newAccessToken, newRefreshToken, userId);
  }
}
