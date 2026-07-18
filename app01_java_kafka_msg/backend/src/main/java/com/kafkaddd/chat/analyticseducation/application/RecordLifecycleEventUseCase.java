package com.kafkaddd.chat.analyticseducation.application;

import com.kafkaddd.chat.analyticseducation.domain.LifecycleEvent;
import org.springframework.stereotype.Service;

/**
 * Records one observed {@link LifecycleEvent}: republishes it as durable
 * telemetry and pushes it live to the education dashboard. Deliberately has
 * no branching logic of its own — this context never decides *whether* to
 * act on an event (that would require understanding Chat Delivery's domain
 * rules, which would violate the bounded-context separation), it just
 * relays whatever it observed.
 */
@Service
public class RecordLifecycleEventUseCase {

  private final LifecycleEventPublisher publisher;
  private final LifecycleEventNotifier notifier;

  public RecordLifecycleEventUseCase(LifecycleEventPublisher publisher, LifecycleEventNotifier notifier) {
    this.publisher = publisher;
    this.notifier = notifier;
  }

  public void record(LifecycleEvent event) {
    publisher.publish(event);
    notifier.notify(event);
  }
}
