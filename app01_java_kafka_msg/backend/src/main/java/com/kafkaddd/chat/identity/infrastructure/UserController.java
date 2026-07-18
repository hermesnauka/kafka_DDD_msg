package com.kafkaddd.chat.identity.infrastructure;

import com.kafkaddd.chat.identity.application.CurrentUserQuery;
import com.kafkaddd.chat.identity.domain.UserId;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
class UserController {

  private final CurrentUserQuery currentUserQuery;

  UserController(CurrentUserQuery currentUserQuery) {
    this.currentUserQuery = currentUserQuery;
  }

  @GetMapping("/me")
  UserResponse me(Authentication authentication) {
    // Principal is the UserId JwtAuthenticationFilter put there after
    // verifying the access-token cookie — SecurityConfig guarantees this
    // method never runs without one.
    UserId userId = (UserId) authentication.getPrincipal();
    return UserResponse.from(currentUserQuery.getById(userId));
  }
}
