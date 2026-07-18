package com.kafkaddd.chat.identity.infrastructure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds {@code app.security.jwt.*} (application.yml). {@code secret} is
 * expected to be a Base64-encoded random key (see application.yml's
 * comment on {@code JWT_SECRET} — no default is provided anywhere, on
 * purpose).
 */
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
record JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {}
