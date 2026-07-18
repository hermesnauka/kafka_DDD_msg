package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.Email;
import com.kafkaddd.chat.identity.domain.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Finds another user by email — the "who do I start a chat with" lookup a
 * create-room UI needs, distinct from {@link CurrentUserQuery} (which is
 * about the caller's own profile, keyed by their already-known {@code UserId}).
 */
@Service
public class UserLookupQuery {

  private final UserRepository userRepository;

  public UserLookupQuery(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserView getByEmail(String email) {
    return userRepository.findByEmail(new Email(email)).map(UserView::from).orElseThrow(NoSuchUserException::new);
  }
}
