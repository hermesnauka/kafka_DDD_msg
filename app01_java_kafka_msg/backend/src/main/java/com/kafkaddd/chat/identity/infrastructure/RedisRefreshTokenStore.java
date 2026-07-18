package com.kafkaddd.chat.identity.infrastructure;

import com.kafkaddd.chat.identity.application.RefreshTokenStore;
import com.kafkaddd.chat.identity.domain.UserId;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed {@link RefreshTokenStore} (SR-2's rotation requirement).
 * Two key shapes:
 *
 * <ul>
 *   <li>{@code refresh-token:<token>} -&gt; userId, TTL = refreshTokenTtl —
 *       the token itself, deleted the instant it's consumed.
 *   <li>{@code user-refresh-tokens:<userId>} -&gt; set of that user's
 *       currently-active tokens, so {@link #revokeAll} can find and delete
 *       them all (e.g. on logout).
 * </ul>
 */
@Component
class RedisRefreshTokenStore implements RefreshTokenStore {

  private static final String TOKEN_KEY_PREFIX = "refresh-token:";
  private static final String USER_TOKENS_KEY_PREFIX = "user-refresh-tokens:";

  private final StringRedisTemplate redis;
  private final JwtProperties properties;
  private final SecureRandom random = new SecureRandom();

  RedisRefreshTokenStore(StringRedisTemplate redis, JwtProperties properties) {
    this.redis = redis;
    this.properties = properties;
  }

  @Override
  public String issue(UserId userId) {
    String token = newOpaqueToken();
    redis.opsForValue().set(TOKEN_KEY_PREFIX + token, userId.value().toString(), properties.refreshTokenTtl());
    String userTokensKey = USER_TOKENS_KEY_PREFIX + userId.value();
    redis.opsForSet().add(userTokensKey, token);
    redis.expire(userTokensKey, properties.refreshTokenTtl());
    return token;
  }

  @Override
  public Optional<UserId> consume(String refreshToken) {
    String tokenKey = TOKEN_KEY_PREFIX + refreshToken;
    String userId = redis.opsForValue().get(tokenKey);
    if (userId == null) {
      return Optional.empty();
    }
    redis.delete(tokenKey);
    redis.opsForSet().remove(USER_TOKENS_KEY_PREFIX + userId, refreshToken);
    return Optional.of(new UserId(UUID.fromString(userId)));
  }

  @Override
  public void revokeAll(UserId userId) {
    String userTokensKey = USER_TOKENS_KEY_PREFIX + userId.value();
    Set<String> tokens = redis.opsForSet().members(userTokensKey);
    if (tokens != null) {
      tokens.forEach(token -> redis.delete(TOKEN_KEY_PREFIX + token));
    }
    redis.delete(userTokensKey);
  }

  private String newOpaqueToken() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
