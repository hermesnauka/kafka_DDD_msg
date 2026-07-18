package com.kafkaddd.chat.analyticseducation.infrastructure;

import com.kafkaddd.chat.analyticseducation.application.RecordLifecycleEventUseCase;
import com.kafkaddd.chat.analyticseducation.domain.LifecycleEvent;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

/**
 * Binds a {@code Consumer<Message<ChatRoomEventEnvelope>>} to
 * {@code chat.room.events} under this context's own consumer group
 * ({@code analytics-education-service} — see application.yml), completely
 * independent of Chat Delivery's {@code chat-delivery-service} group:
 * Kafka consumer groups each track their own offset into the topic, so
 * this context observing the topic never interferes with Chat Delivery's
 * own consumption of it (NFR-Arch-1's "never in the critical path").
 *
 * <p>Uses the {@code Message<T>} form (rather than a bare payload) to read
 * {@code KafkaHeaders.RECEIVED_PARTITION}/{@code KafkaHeaders.OFFSET} —
 * the partition/offset metadata the education dashboard visualizes
 * (US-2.2) isn't in the event payload itself, only in the Kafka record's
 * own metadata.
 */
@Configuration
class ChatRoomEventsObserverConfig {

  private static final String CONSUMER_GROUP = "analytics-education-service";

  @Bean
  Consumer<Message<ChatRoomEventEnvelope>> educationConsumer(RecordLifecycleEventUseCase useCase) {
    return message -> {
      ChatRoomEventEnvelope envelope = message.getPayload();
      int partition = (int) message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION);
      long offset = (long) message.getHeaders().get(KafkaHeaders.OFFSET);
      useCase.record(
          new LifecycleEvent(
              envelope.eventType(), envelope.roomId(), envelope.messageId(), partition, offset, CONSUMER_GROUP,
              envelope.occurredAt()));
    };
  }
}
