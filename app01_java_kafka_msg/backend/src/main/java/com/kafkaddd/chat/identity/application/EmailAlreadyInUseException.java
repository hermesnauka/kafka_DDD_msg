package com.kafkaddd.chat.identity.application;

/** Thrown by {@link RegisterUserUseCase} when the email is already registered. */
public class EmailAlreadyInUseException extends RuntimeException {

  public EmailAlreadyInUseException() {
    super("email is already registered");
  }
}
