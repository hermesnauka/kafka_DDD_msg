package com.kafkaddd.chat.identity.infrastructure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.security.jwt.*} (application.yml). {@code secret} is
 * expected to be a Base64-encoded random key (see application.yml's
 * comment on {@code JWT_SECRET} — no default is provided anywhere, on
 * purpose).
 *
 * <p>Registered via {@code @ConfigurationPropertiesScan} on
 * {@link com.kafkaddd.chat.Application}, not {@code @Component} — a record
 * needs Spring's constructor-binding path (which
 * {@code @ConfigurationPropertiesScan} wires up), not the reflective
 * setter-based binding normal {@code @Component} beans get.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
record JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {}
