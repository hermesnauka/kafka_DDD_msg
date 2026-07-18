package com.kafkaddd.chat.identity.application;

/** Thrown by {@link RefreshTokenUseCase} when the refresh token is missing, expired, or already used. */
public class InvalidRefreshTokenException extends RuntimeException {

  public InvalidRefreshTokenException() {
    super("invalid or expired refresh token");
  }
}
