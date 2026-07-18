package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.User;
import com.kafkaddd.chat.identity.domain.UserId;

/** Read-only projection of a {@link User} for API responses — never carries the password hash. */
public record UserView(UserId id, String email, String displayName) {

  public static UserView from(User user) {
    return new UserView(user.id(), user.email().value(), user.displayName().value());
  }
}
