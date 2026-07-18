package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.UserId;
import java.util.Optional;

/**
 * Port for issuing and rotating refresh tokens (SR-2). {@link #consume}
 * must be single-use: a valid token is deleted the moment it's redeemed,
 * so a stolen-but-already-rotated-out refresh token can never be replayed.
 * The real Redis-backed implementation lives in
 * {@code identity.infrastructure}.
 */
public interface RefreshTokenStore {

  String issue(UserId userId);

  /** Validates and invalidates {@code refreshToken} in one step (rotation). */
  Optional<UserId> consume(String refreshToken);

  /** Invalidates every refresh token issued to {@code userId} (e.g. on logout). */
  void revokeAll(UserId userId);
}
