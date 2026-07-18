package com.kafkaddd.chat.chatdelivery.domain;

/**
 * The body of a chat message. Validated at construction so an invalid
 * message can never exist inside the aggregate (SR-4's input-validation
 * requirement enforced at the domain boundary, in addition to the API
 * layer's Bean Validation).
 */
public record MessageContent(String value) {

  public static final int MAX_LENGTH = 4000;

  public MessageContent {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("message content must not be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "message content must not exceed " + MAX_LENGTH + " characters");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
