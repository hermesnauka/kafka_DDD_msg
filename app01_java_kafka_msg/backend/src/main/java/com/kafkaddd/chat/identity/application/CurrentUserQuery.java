package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.UserId;
import com.kafkaddd.chat.identity.domain.UserRepository;
import org.springframework.stereotype.Service;

/** Backs {@code GET /api/v1/users/me} — the userId comes from an already-verified access token. */
@Service
public class CurrentUserQuery {

  private final UserRepository userRepository;

  public CurrentUserQuery(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserView getById(UserId userId) {
    return userRepository.findById(userId).map(UserView::from).orElseThrow(NoSuchUserException::new);
  }
}
