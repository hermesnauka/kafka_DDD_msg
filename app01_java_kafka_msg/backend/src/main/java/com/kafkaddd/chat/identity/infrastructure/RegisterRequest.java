package com.kafkaddd.chat.identity.infrastructure;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** SR-4: validated at the API boundary, in addition to the domain's own VO validation. */
record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 72) String password,
    @NotBlank @Size(max = 50) String displayName) {}
