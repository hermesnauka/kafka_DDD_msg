package com.kafkaddd.chat.chatdelivery.infrastructure;

import jakarta.validation.constraints.NotBlank;

record SendMessageRequest(@NotBlank String content) {}
