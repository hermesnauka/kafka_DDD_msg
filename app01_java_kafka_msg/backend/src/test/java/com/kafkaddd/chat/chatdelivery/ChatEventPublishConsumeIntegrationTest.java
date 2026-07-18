package com.kafkaddd.chat.chatdelivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.kafkaddd.chat.chatdelivery.application.DomainEventPublisher;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.Message;
import com.kafkaddd.chat.chatdelivery.domain.MessageContent;
import com.kafkaddd.chat.chatdelivery.domain.MessageStatus;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.Timestamp;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Real Kafka + real Postgres (Testcontainers) — proves the
 * StreamBridge-publish / functional-consumer wiring in application.yml
 * actually matches at runtime (binding names, content-type, consumer
 * group). This is exactly the class of bug a mocked
 * {@link DomainEventPublisher} test can't catch — Spring Cloud Stream
 * binding misconfiguration only fails when something really tries to
 * connect.
 */
@Testcontainers
@SpringBootTest(properties = "app.security.jwt.secret=dGVzdC1vbmx5LXNlY3JldC1uZXZlci11c2UtaW4tcHJvZC10ZXN0")
class ChatEventPublishConsumeIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.9.8"));

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
  }

  @Autowired private ChatRoomRepository chatRoomRepository;
  @Autowired private DomainEventPublisher eventPublisher;

  @Test
  void aPublishedMessageSentEventIsConsumedAndDrivesTheMessageToDelivered() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    Timestamp now = Timestamp.now();

    ChatRoom room = ChatRoom.create(roomId, List.of(alice), now);
    Message message = room.sendMessage(alice, new MessageContent("hi"), now);
    chatRoomRepository.save(room);
    room.pullDomainEvents().forEach(eventPublisher::publish);

    await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(
            () -> {
              ChatRoom reloaded = chatRoomRepository.findById(roomId).orElseThrow();
              Message reloadedMessage =
                  reloaded.messages().stream().filter(m -> m.id().equals(message.id())).findFirst().orElseThrow();
              assertThat(reloadedMessage.status()).isEqualTo(MessageStatus.DELIVERED);
            });
  }
}
