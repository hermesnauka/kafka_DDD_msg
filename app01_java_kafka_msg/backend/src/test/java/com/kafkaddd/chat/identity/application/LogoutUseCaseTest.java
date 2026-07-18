package com.kafkaddd.chat.identity.application;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.kafkaddd.chat.identity.domain.UserId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

  @Mock private RefreshTokenStore refreshTokenStore;

  @Test
  void logoutRevokesEveryRefreshTokenForTheUser() {
    UserId userId = new UserId(UUID.randomUUID());

    new LogoutUseCase(refreshTokenStore).logout(userId);

    verify(refreshTokenStore, times(1)).revokeAll(userId);
  }
}
