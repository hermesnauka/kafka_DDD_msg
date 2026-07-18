package com.kafkaddd.chat.identity.domain;

/**
 * Thrown when authentication is attempted against an account that's
 * currently locked out after too many failed attempts (SR-1's throttling
 * requirement).
 */
public class AccountLockedException extends RuntimeException {

  public AccountLockedException(UserId userId, Timestamp lockedUntil) {
    super("account " + userId + " is locked until " + lockedUntil);
  }
}
