package com.kafkaddd.chat.identity.domain;

/**
 * An already-hashed password. Never constructed from a raw password
 * directly — only {@link PasswordHasher#hash} produces one, so a plaintext
 * password can never accidentally be persisted as if it were hashed.
 */
public record HashedPassword(String value) {

  public HashedPassword {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("hashed password must not be blank");
    }
  }

  @Override
  public String toString() {
    // Never expose the hash in logs/error messages, even accidentally via
    // string concatenation or a debugger's toString() call.
    return "HashedPassword[REDACTED]";
  }
}
