package com.kafkaddd.chat.analyticseducation.application;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.kafkaddd.chat.analyticseducation.domain.LifecycleEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordLifecycleEventUseCaseTest {

  @Mock private LifecycleEventPublisher publisher;
  @Mock private LifecycleEventNotifier notifier;

  @Test
  void recordingAnEventPublishesItAsTelemetryAndPushesItLive() {
    LifecycleEvent event =
        new LifecycleEvent("MessageSent", "room-1", "message-1", 0, 42L, "analytics-education-service", Instant.now());

    new RecordLifecycleEventUseCase(publisher, notifier).record(event);

    verify(publisher, times(1)).publish(event);
    verify(notifier, times(1)).notify(event);
  }
}
