package com.kafkaddd.chat.identity.infrastructure;

import com.kafkaddd.chat.identity.application.AccessTokenIssuer;
import com.kafkaddd.chat.identity.domain.UserId;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/** JWT-backed {@link AccessTokenIssuer} (SR-2). */
@Component
class JwtAccessTokenIssuer implements AccessTokenIssuer {

  private final SecretKey key;
  private final JwtProperties properties;

  JwtAccessTokenIssuer(JwtProperties properties) {
    this.properties = properties;
    this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.secret()));
  }

  @Override
  public String issue(UserId userId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId.value().toString())
        .issuedAt(java.util.Date.from(now))
        .expiration(java.util.Date.from(now.plus(properties.accessTokenTtl())))
        .signWith(key)
        .compact();
  }

  @Override
  public Optional<UserId> verify(String token) {
    try {
      String subject = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
      return Optional.of(new UserId(UUID.fromString(subject)));
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
