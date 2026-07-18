package com.kafkaddd.chat.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kafkaddd.chat.identity.application.InvalidCredentialsException;
import com.kafkaddd.chat.identity.application.LoginUseCase;
import com.kafkaddd.chat.identity.application.RegisterUserUseCase;
import com.kafkaddd.chat.identity.domain.AccountLockedException;
import com.kafkaddd.chat.identity.domain.Email;
import com.kafkaddd.chat.identity.domain.User;
import com.kafkaddd.chat.identity.domain.UserRepository;
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
 * A real Spring context + a real Postgres (Testcontainers) — the "simulator"
 * tier from PLAN_SSDLC.md §10, for the one thing a mocked-repository unit
 * test structurally cannot verify: whether {@link LoginUseCase}'s state
 * change actually survives the {@code @Transactional} boundary when it
 * also throws. (It didn't, the first time this was written — see
 * {@code noRollbackFor} on {@link LoginUseCase#login}.)
 *
 * <p>Also spins up a Kafka container even though this test has nothing to
 * do with Chat Delivery: the shared {@code Application} context always
 * wires {@code chatRoomEventsConsumer-in-0}, and setting
 * {@code spring.cloud.function.definition=} empty does *not* skip that
 * binding (Spring Cloud Function treats blank the same as "not set" and
 * auto-detects the one available {@code Consumer} bean regardless) — so
 * without a reachable broker, context startup stalls for ~60s retrying
 * against {@code localhost:9092}.
 */
@Testcontainers
@SpringBootTest(properties = "app.security.jwt.secret=dGVzdC1vbmx5LXNlY3JldC1uZXZlci11c2UtaW4tcHJvZC10ZXN0")
class LoginPersistenceIntegrationTest {

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

  @Autowired private RegisterUserUseCase registerUserUseCase;
  @Autowired private LoginUseCase loginUseCase;
  @Autowired private UserRepository userRepository;

  @Test
  void aFailedLoginAttemptIsPersistedEvenThoughLoginThrows() {
    registerUserUseCase.register("bob@example.com", "correct-horse-battery", "Bob");

    assertThatThrownBy(() -> loginUseCase.login("bob@example.com", "wrong"))
        .isInstanceOf(InvalidCredentialsException.class);
    assertThatThrownBy(() -> loginUseCase.login("bob@example.com", "wrong-again"))
        .isInstanceOf(InvalidCredentialsException.class);

    User reloaded = userRepository.findByEmail(new Email("bob@example.com")).orElseThrow();
    assertThat(reloaded.failedLoginAttempts()).isEqualTo(2);
  }

  @Test
  void anAccountLocksAfterTheThresholdAndThatStateIsPersistedToo() {
    registerUserUseCase.register("carol@example.com", "correct-horse-battery", "Carol");

    for (int i = 0; i < User.MAX_FAILED_ATTEMPTS; i++) {
      assertThatThrownBy(() -> loginUseCase.login("carol@example.com", "wrong"))
          .isInstanceOf(InvalidCredentialsException.class);
    }

    assertThatThrownBy(() -> loginUseCase.login("carol@example.com", "correct-horse-battery"))
        .isInstanceOf(AccountLockedException.class);

    User reloaded = userRepository.findByEmail(new Email("carol@example.com")).orElseThrow();
    assertThat(reloaded.failedLoginAttempts()).isEqualTo(User.MAX_FAILED_ATTEMPTS);
    assertThat(reloaded.lockedUntil()).isNotNull();
  }
}
