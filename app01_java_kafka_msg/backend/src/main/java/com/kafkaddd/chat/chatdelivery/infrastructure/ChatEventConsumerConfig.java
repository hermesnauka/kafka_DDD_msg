package com.kafkaddd.chat.chatdelivery.infrastructure;

import com.kafkaddd.chat.chatdelivery.application.ProcessMessageSentUseCase;
import com.kafkaddd.chat.chatdelivery.domain.MessageId;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Binds a {@code Consumer<ChatEventEnvelope>} to {@code chat.room.events}
 * (bean name {@code chatRoomEventsConsumer} maps to the
 * {@code chatRoomEventsConsumer-in-0} binding by Spring Cloud Stream's
 * functional naming convention — see application.yml).
 *
 * <p>This consumer group only reacts to {@code "MessageSent"}; {@code
 * MessagePolled}/{@code MessageDelivered} on this same topic are metadata
 * for a future Analytics &amp; Education consumer, not something this
 * service's own delivery pipeline needs to act on (they're the events
 * *this* consumer just produced, read back — ignoring them here also
 * avoids an infinite reprocessing loop).
 */
@Configuration
class ChatEventConsumerConfig {

  @Bean
  Consumer<ChatEventEnvelope> chatRoomEventsConsumer(ProcessMessageSentUseCase processMessageSentUseCase) {
    return envelope -> {
      if (!"MessageSent".equals(envelope.eventType())) {
        return;
      }
      RoomId roomId = new RoomId(UUID.fromString(envelope.roomId()));
      MessageId messageId = new MessageId(UUID.fromString(envelope.messageId()));
      processMessageSentUseCase.process(roomId, messageId);
    };
  }
}
