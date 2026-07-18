package com.kafkaddd.chat.chatdelivery.domain;

/** Thrown when a {@link RoomId} doesn't refer to any known room. */
public class NoSuchRoomException extends RuntimeException {

  public NoSuchRoomException(RoomId roomId) {
    super("no such room: " + roomId);
  }
}
