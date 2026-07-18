package com.kafkaddd.chat.identity.infrastructure;

import com.kafkaddd.chat.identity.application.CurrentUserQuery;
import com.kafkaddd.chat.identity.application.UserLookupQuery;
import com.kafkaddd.chat.identity.domain.UserId;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
class UserController {

  private final CurrentUserQuery currentUserQuery;
  private final UserLookupQuery userLookupQuery;

  UserController(CurrentUserQuery currentUserQuery, UserLookupQuery userLookupQuery) {
    this.currentUserQuery = currentUserQuery;
    this.userLookupQuery = userLookupQuery;
  }

  @GetMapping("/me")
  UserResponse me(Authentication authentication) {
    // Principal is the UserId JwtAuthenticationFilter put there after
    // verifying the access-token cookie — SecurityConfig guarantees this
    // method never runs without one.
    UserId userId = (UserId) authentication.getPrincipal();
    return UserResponse.from(currentUserQuery.getById(userId));
  }

  /**
   * Looks up another user by email — used by the create-room UI to turn
   * "chat with alice@example.com" into the {@code UserId} the
   * {@code POST /api/v1/rooms} request body actually needs. Requires
   * authentication (not in SecurityConfig's permitAll list) like every
   * other endpoint here, so it can't be used to enumerate accounts by an
   * unauthenticated caller — but note it still confirms whether *any* given
   * email is registered to any authenticated caller, which is a narrower,
   * accepted tradeoff versus the login endpoint's stricter
   * enumeration-resistance (InvalidCredentialsException's collapsed
   * messaging).
   */
  @GetMapping("/by-email")
  UserResponse byEmail(@RequestParam String email) {
    return UserResponse.from(userLookupQuery.getByEmail(email));
  }
}
