package com.kafkaddd.chat.identity.infrastructure;

import com.kafkaddd.chat.identity.application.AccessTokenIssuer;
import com.kafkaddd.chat.identity.domain.UserId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads the {@value AuthCookies#ACCESS_TOKEN_COOKIE} cookie, verifies it,
 * and — if valid — sets the request's {@link org.springframework.security.core.Authentication}
 * principal to the token's {@link UserId} (SR-2, SR-10). A missing or
 * invalid token simply leaves the security context empty; {@link SecurityConfig}'s
 * {@code anyRequest().authenticated()} is what actually rejects the
 * request for protected endpoints.
 */
@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final AccessTokenIssuer accessTokenIssuer;

  JwtAuthenticationFilter(AccessTokenIssuer accessTokenIssuer) {
    this.accessTokenIssuer = accessTokenIssuer;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    findCookie(request, AuthCookies.ACCESS_TOKEN_COOKIE)
        .flatMap(accessTokenIssuer::verify)
        .ifPresent(
            userId ->
                SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, List.of())));
    chain.doFilter(request, response);
  }

  private Optional<String> findCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    return List.of(cookies).stream().filter(c -> c.getName().equals(name)).map(Cookie::getValue).findFirst();
  }
}
