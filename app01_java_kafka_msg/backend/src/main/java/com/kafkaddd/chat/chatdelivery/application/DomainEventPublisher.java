package com.kafkaddd.chat.chatdelivery.application;

import com.kafkaddd.chat.chatdelivery.domain.DomainEvent;

/**
 * Port for publishing a {@link DomainEvent} to {@code chat.room.events}
 * (PLAN_SSDLC.md §7.3). The real Kafka-backed adapter lives in
 * {@code chatdelivery.infrastructure}.
 */
public interface DomainEventPublisher {

  void publish(DomainEvent event);
}
