package com.kafkaddd.chat.chatdelivery.infrastructure;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** {@code participantIds} are the *other* participants — the caller is always added too. */
record CreateRoomRequest(@NotNull List<String> participantIds) {}
