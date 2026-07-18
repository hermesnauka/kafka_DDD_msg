package com.kafkaddd.chat.analyticseducation.application;

import com.kafkaddd.chat.analyticseducation.domain.LifecycleEvent;

/**
 * Port for publishing a {@link LifecycleEvent} to {@code educational.telemetry}
 * (PLAN_SSDLC.md §7.3). The real Kafka-backed adapter lives in
 * {@code analyticseducation.infrastructure}.
 */
public interface LifecycleEventPublisher {

  void publish(LifecycleEvent event);
}
