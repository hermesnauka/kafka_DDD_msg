package com.kafkaddd.chat.identity.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Placeholder security config: only {@code /actuator/health} and
 * {@code /actuator/info} are public; everything else requires
 * authentication. Stateless session policy matches the JWT-based auth
 * planned for this context (SR-2) — no HTTP Basic/form login, since the
 * real auth mechanism is a JWT filter added alongside FR-1's
 * register/login endpoints, not yet implemented.
 *
 * <p>CSRF is disabled because there are no session-cookie-authenticated,
 * state-changing endpoints yet; re-evaluate when those are added (a
 * stateless JWT API with no cookies isn't CSRF-vulnerable, but this needs
 * re-checking once auth endpoints exist).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info")
                    .permitAll()
                    .anyRequest()
                    .authenticated());
    return http.build();
  }
}
