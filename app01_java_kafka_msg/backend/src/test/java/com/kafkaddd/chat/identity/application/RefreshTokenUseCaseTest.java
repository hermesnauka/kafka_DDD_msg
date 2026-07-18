package com.kafkaddd.chat.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.kafkaddd.chat.identity.domain.UserId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

  @Mock private AccessTokenIssuer accessTokenIssuer;
  @Mock private RefreshTokenStore refreshTokenStore;

  private RefreshTokenUseCase useCase() {
    return new RefreshTokenUseCase(accessTokenIssuer, refreshTokenStore);
  }

  @Test
  void anInvalidOrAlreadyUsedTokenIsRejected() {
    when(refreshTokenStore.consume("bad-token")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase().refresh("bad-token")).isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void aValidTokenIsRotatedForAFreshPair() {
    UserId userId = new UserId(UUID.randomUUID());
    when(refreshTokenStore.consume("old-token")).thenReturn(Optional.of(userId));
    when(accessTokenIssuer.issue(userId)).thenReturn("new-access-token");
    when(refreshTokenStore.issue(userId)).thenReturn("new-refresh-token");

    AuthResult result = useCase().refresh("old-token");

    assertThat(result.accessToken()).isEqualTo("new-access-token");
    assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
    assertThat(result.userId()).isEqualTo(userId);
  }
}
