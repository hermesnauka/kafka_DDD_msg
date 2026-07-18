package com.kafkaddd.chat.chatdelivery;

import static org.assertj.core.api.Assertions.assertThat;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.Message;
import com.kafkaddd.chat.chatdelivery.domain.MessageContent;
import com.kafkaddd.chat.chatdelivery.domain.MessageStatus;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.Timestamp;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
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
 * Real Spring context + real Postgres (Testcontainers) — the tier that
 * catches what mocked-repository tests structurally can't: whether the
 * three-table mapping in {@code ChatRoomRepositoryAdapter} round-trips a
 * full aggregate correctly, and whether re-saving after a status
 * transition updates in place instead of duplicating rows.
 *
 * <p>Also spins up Kafka: the shared {@code Application} context always
 * wires {@code chatRoomEventsConsumer-in-0}, and without a reachable
 * broker context startup stalls for ~60s retrying against
 * {@code localhost:9092} (see {@code LoginPersistenceIntegrationTest}'s
 * class doc for why {@code spring.cloud.function.definition=} doesn't
 * avoid this).
 */
@Testcontainers
@SpringBootTest(properties = "app.security.jwt.secret=dGVzdC1vbmx5LXNlY3JldC1uZXZlci11c2UtaW4tcHJvZC10ZXN0")
class ChatRoomRepositoryIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.9.8"));

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
  }

  @Autowired private ChatRoomRepository repository;

  @Test
  void roundTripsAFullAggregateAndListsItForEachParticipant() {
    UserId alice = new UserId(UUID.randomUUID());
    UserId bob = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    Timestamp now = Timestamp.now();

    ChatRoom room = ChatRoom.create(roomId, List.of(alice, bob), now);
    Message message = room.sendMessage(alice, new MessageContent("hi bob"), now);
    repository.save(room);

    ChatRoom reloaded = repository.findById(roomId).orElseThrow();
    assertThat(reloaded.participants()).hasSize(2);
    assertThat(reloaded.isParticipant(alice)).isTrue();
    assertThat(reloaded.isParticipant(bob)).isTrue();
    assertThat(reloaded.messages()).hasSize(1);
    assertThat(reloaded.messages().iterator().next().id()).isEqualTo(message.id());
    assertThat(reloaded.messages().iterator().next().status()).isEqualTo(MessageStatus.PENDING);

    assertThat(repository.findRoomIdsForParticipant(alice)).containsExactly(roomId);
    assertThat(repository.findRoomIdsForParticipant(bob)).containsExactly(roomId);
  }

  @Test
  void resavingAfterAStatusTransitionUpdatesInPlaceWithoutDuplicatingParticipants() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    Timestamp now = Timestamp.now();

    ChatRoom room = ChatRoom.create(roomId, List.of(alice), now);
    Message message = room.sendMessage(alice, new MessageContent("hi"), now);
    repository.save(room);

    ChatRoom reloaded = repository.findById(roomId).orElseThrow();
    reloaded.markPolled(message.id(), now);
    reloaded.markDelivered(message.id(), now);
    repository.save(reloaded);

    ChatRoom reloadedAgain = repository.findById(roomId).orElseThrow();
    assertThat(reloadedAgain.participants()).hasSize(1);
    assertThat(reloadedAgain.messages()).hasSize(1);
    assertThat(reloadedAgain.messages().iterator().next().status()).isEqualTo(MessageStatus.DELIVERED);
  }
}
