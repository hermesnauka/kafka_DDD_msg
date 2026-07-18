package com.kafkaddd.chat.identity.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * {@code /actuator/health}, {@code /actuator/info}, and {@code /api/v1/auth/**}
 * (register/login/refresh, which must be reachable before a caller has any
 * token) are public; everything else requires the {@code access_token}
 * cookie ({@link JwtAuthenticationFilter}, SR-2/SR-10). Stateless session
 * policy — no HTTP Basic/form login, no server-side session.
 *
 * <p>CSRF is disabled: both auth cookies are issued with {@code SameSite=Strict}
 * ({@link AuthCookies}), so the browser never attaches them to a cross-site
 * request in the first place — the scenario CSRF protection exists to stop.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/actuator/health", "/actuator/health/**", "/actuator/info", "/api/v1/auth/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
