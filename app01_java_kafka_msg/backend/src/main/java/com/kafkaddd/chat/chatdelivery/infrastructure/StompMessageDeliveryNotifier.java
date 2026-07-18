package com.kafkaddd.chat.chatdelivery.infrastructure;

import com.kafkaddd.chat.chatdelivery.application.MessageDeliveryNotifier;
import com.kafkaddd.chat.chatdelivery.domain.Message;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes a delivered message to every client subscribed to {@code /topic/rooms/{roomId}}. */
@Component
class StompMessageDeliveryNotifier implements MessageDeliveryNotifier {

  private final SimpMessagingTemplate messagingTemplate;

  StompMessageDeliveryNotifier(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  public void notifyDelivered(RoomId roomId, Message message) {
    messagingTemplate.convertAndSend("/topic/rooms/" + roomId.value(), MessageResponse.from(message));
  }
}
