package com.kafkaddd.chat.identity.application;

/**
 * Thrown by {@link LoginUseCase} for both "no such email" and "wrong
 * password" — deliberately the same exception/message for both, so the API
 * never discloses whether an email is registered (avoids user enumeration).
 */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("invalid email or password");
  }
}
