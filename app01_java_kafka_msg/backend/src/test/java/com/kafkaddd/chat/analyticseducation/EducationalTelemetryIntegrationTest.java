package com.kafkaddd.chat.analyticseducation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkaddd.chat.chatdelivery.application.DomainEventPublisher;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.MessageContent;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.Timestamp;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
 * Real Kafka + real Postgres — proves the whole observation pipeline: a
 * message sent through Chat Delivery ends up, via Analytics &amp; Education's
 * *independent* consumer group on {@code chat.room.events}
 * ({@link com.kafkaddd.chat.analyticseducation.infrastructure.ChatRoomEventsObserverConfig}),
 * republished onto {@code educational.telemetry} — and confirms the
 * FR-6 metadata-only boundary: no message content ever appears there,
 * even though the underlying {@code MessageSent} event carries it.
 */
@Testcontainers
@SpringBootTest(properties = "app.security.jwt.secret=dGVzdC1vbmx5LXNlY3JldC1uZXZlci11c2UtaW4tcHJvZC10ZXN0")
class EducationalTelemetryIntegrationTest {

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

  private KafkaConsumer<String, String> telemetryConsumer;

  @BeforeEach
  void subscribeToTelemetryTopic() {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-telemetry-observer-" + UUID.randomUUID());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    telemetryConsumer = new KafkaConsumer<>(props);
    telemetryConsumer.subscribe(List.of("educational.telemetry"));
  }

  @AfterEach
  void closeConsumer() {
    telemetryConsumer.close();
  }

  @Test
  void sendingAMessageProducesMetadataOnlyTelemetryForEveryLifecycleStage() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    Timestamp now = Timestamp.now();

    ChatRoom room = ChatRoom.create(roomId, List.of(alice), now);
    room.sendMessage(alice, new MessageContent("this content must never reach educational.telemetry"), now);
    chatRoomRepository.save(room);
    room.pullDomainEvents().forEach(eventPublisher::publish);

    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    List<JsonNode> telemetryEvents = new ArrayList<>();
    await()
        .atMost(Duration.ofSeconds(20))
        .until(
            () -> {
              telemetryEvents.addAll(pollTelemetry(mapper));
              return telemetryEvents.size() >= 3;
            });

    Set<String> eventTypes = telemetryEvents.stream().map(n -> n.get("eventType").asText()).collect(Collectors.toSet());
    assertThat(eventTypes).containsExactlyInAnyOrder("MessageSent", "MessagePolled", "MessageDelivered");

    assertThat(telemetryEvents)
        .allSatisfy(
            node -> {
              assertThat(node.get("roomId").asText()).isEqualTo(roomId.value().toString());
              assertThat(node.get("consumerGroup").asText()).isEqualTo("analytics-education-service");
              assertThat(node.has("content")).as("telemetry must never carry message content (FR-6)").isFalse();
              assertThat(node.toString()).doesNotContain("this content must never reach educational.telemetry");
            });
  }

  private List<JsonNode> pollTelemetry(ObjectMapper mapper) {
    ConsumerRecords<String, String> records = telemetryConsumer.poll(Duration.ofMillis(500));
    List<JsonNode> result = new ArrayList<>();
    for (ConsumerRecord<String, String> record : records.records("educational.telemetry")) {
      try {
        result.add(mapper.readTree(record.value()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }
}
