package com.kafkaddd.chat.chatdelivery.domain;

/** Thrown when a {@link MessageId} doesn't refer to a message in the given {@link ChatRoom}. */
public class NoSuchMessageException extends RuntimeException {

  public NoSuchMessageException(RoomId roomId, MessageId messageId) {
    super("room " + roomId + " has no message " + messageId);
  }
}
