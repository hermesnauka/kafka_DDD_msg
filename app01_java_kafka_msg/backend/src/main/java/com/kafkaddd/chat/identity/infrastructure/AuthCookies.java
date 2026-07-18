package com.kafkaddd.chat.identity.infrastructure;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Builds the {@code HttpOnly}/{@code Secure}/{@code SameSite} cookies that
 * carry the access and refresh tokens (SR-2 — tokens are never put in a
 * response body for JS/localStorage to see).
 *
 * <p>{@code Secure} is always on, per SR-2, which means these cookies are
 * only stored by a browser talking to this API over HTTPS; local dev over
 * plain HTTP needs a TLS-terminating reverse proxy in front of it (not yet
 * set up — curl-based smoke tests bypass this since curl doesn't enforce
 * browser cookie-jar semantics).
 */
@Component
class AuthCookies {

  static final String ACCESS_TOKEN_COOKIE = "access_token";
  static final String REFRESH_TOKEN_COOKIE = "refresh_token";

  private final JwtProperties properties;

  AuthCookies(JwtProperties properties) {
    this.properties = properties;
  }

  ResponseCookie accessTokenCookie(String token) {
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(properties.accessTokenTtl())
        .build();
  }

  ResponseCookie refreshTokenCookie(String token) {
    // Scoped to just the auth endpoints that need it, not every request.
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/api/v1/auth")
        .maxAge(properties.refreshTokenTtl())
        .build();
  }

  /** Same attributes as {@link #accessTokenCookie}, but empty and immediately expired — clears it browser-side. */
  ResponseCookie clearedAccessTokenCookie() {
    return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(0)
        .build();
  }

  /** Same attributes as {@link #refreshTokenCookie}, but empty and immediately expired — clears it browser-side. */
  ResponseCookie clearedRefreshTokenCookie() {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/api/v1/auth")
        .maxAge(0)
        .build();
  }
}
