package com.kafkaddd.chat.identity.infrastructure;

import com.kafkaddd.chat.identity.application.UserView;

/** Never carries the password hash — see {@link UserView}. */
record UserResponse(String id, String email, String displayName) {

  static UserResponse from(UserView view) {
    return new UserResponse(view.id().toString(), view.email(), view.displayName());
  }
}
