package com.kafkaddd.chat.chatdelivery.infrastructure;

import com.kafkaddd.chat.chatdelivery.application.MessageView;
import com.kafkaddd.chat.chatdelivery.domain.Message;

/** REST/STOMP payload shape for a message (PLAN_SSDLC.md §7.1/§7.2). */
record MessageResponse(String id, String senderId, String content, String sentAt, String status) {

  static MessageResponse from(Message message) {
    return new MessageResponse(
        message.id().toString(),
        message.senderId().toString(),
        message.content().value(),
        message.sentAt().toString(),
        message.status().name());
  }

  static MessageResponse from(MessageView view) {
    return new MessageResponse(view.id(), view.senderId(), view.content(), view.sentAt(), view.status());
  }
}
