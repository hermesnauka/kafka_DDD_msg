package com.kafkaddd.chat.chatdelivery.domain;

/** Thrown when a {@link Message} state transition is attempted out of order. */
public class IllegalMessageStateException extends RuntimeException {

  public IllegalMessageStateException(MessageId messageId, MessageStatus actual, MessageStatus expected) {
    super("message " + messageId + " is " + actual + ", expected " + expected + " for this transition");
  }
}
