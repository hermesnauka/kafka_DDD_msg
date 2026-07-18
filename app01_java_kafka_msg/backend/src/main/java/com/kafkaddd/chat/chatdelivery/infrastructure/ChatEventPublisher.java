package com.kafkaddd.chat.chatdelivery.infrastructure;

import com.kafkaddd.chat.chatdelivery.application.DomainEventPublisher;
import com.kafkaddd.chat.chatdelivery.domain.DomainEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/** Publishes to the {@code chat-room-events-out-0} binding (destination {@code chat.room.events}). */
@Component
class ChatEventPublisher implements DomainEventPublisher {

  private static final String BINDING = "chat-room-events-out-0";

  private final StreamBridge streamBridge;

  ChatEventPublisher(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void publish(DomainEvent event) {
    streamBridge.send(BINDING, ChatEventEnvelope.from(event));
  }
}
