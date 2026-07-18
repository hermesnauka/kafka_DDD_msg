package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.UserId;
import java.util.Optional;

/**
 * Port for issuing and verifying short-lived access tokens (SR-2). The
 * real JWT-backed implementation lives in {@code identity.infrastructure}.
 */
public interface AccessTokenIssuer {

  String issue(UserId userId);

  /** Returns the token's subject if it's a validly signed, unexpired token. */
  Optional<UserId> verify(String token);
}
