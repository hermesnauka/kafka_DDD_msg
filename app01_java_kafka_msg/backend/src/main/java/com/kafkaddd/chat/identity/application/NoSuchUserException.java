package com.kafkaddd.chat.identity.application;

/**
 * Thrown when a validated access token's subject no longer maps to a user
 * (e.g. deleted between token issuance and use). Distinct from
 * {@link InvalidCredentialsException} — the token itself was valid.
 */
public class NoSuchUserException extends RuntimeException {

  public NoSuchUserException() {
    super("no such user");
  }
}
