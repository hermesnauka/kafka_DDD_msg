package com.kafkaddd.chat.identity.application;

import com.kafkaddd.chat.identity.domain.UserId;

/** Result of a successful login/refresh: the two tokens the controller puts in cookies. */
public record AuthResult(String accessToken, String refreshToken, UserId userId) {}
