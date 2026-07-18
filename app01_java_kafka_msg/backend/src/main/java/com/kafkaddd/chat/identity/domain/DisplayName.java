package com.kafkaddd.chat.identity.domain;

/** A user's display name, shown to other participants in a chat room. */
public record DisplayName(String value) {

  public static final int MAX_LENGTH = 50;

  public DisplayName {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("display name must not be blank");
    }
    value = value.strip();
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("display name must not exceed " + MAX_LENGTH + " characters");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
