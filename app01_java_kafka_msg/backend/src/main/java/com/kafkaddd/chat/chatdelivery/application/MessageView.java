package com.kafkaddd.chat.chatdelivery.application;

import com.kafkaddd.chat.chatdelivery.domain.Message;

/** Read-only projection of a {@link Message} for API responses. */
public record MessageView(String id, String senderId, String content, String sentAt, String status) {

  public static MessageView from(Message message) {
    return new MessageView(
        message.id().toString(),
        message.senderId().toString(),
        message.content().value(),
        message.sentAt().toString(),
        message.status().name());
  }
}
