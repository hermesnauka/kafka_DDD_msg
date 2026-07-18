package com.kafkaddd.chat.analyticseducation.infrastructure;

import com.kafkaddd.chat.analyticseducation.application.LifecycleEventPublisher;
import com.kafkaddd.chat.analyticseducation.domain.LifecycleEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/** Publishes to the {@code education-telemetry-out-0} binding (destination {@code educational.telemetry}). */
@Component
class LifecycleEventKafkaPublisher implements LifecycleEventPublisher {

  private static final String BINDING = "education-telemetry-out-0";

  private final StreamBridge streamBridge;

  LifecycleEventKafkaPublisher(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void publish(LifecycleEvent event) {
    streamBridge.send(BINDING, event);
  }
}
