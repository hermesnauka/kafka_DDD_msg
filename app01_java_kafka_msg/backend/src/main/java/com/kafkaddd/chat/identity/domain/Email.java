package com.kafkaddd.chat.identity.domain;

import java.util.regex.Pattern;

/**
 * A validated, normalized (lowercased/trimmed) email address. Uniqueness
 * across users is an application-layer concern (it requires querying the
 * repository, which a single value object can't do) — see
 * {@code identity.application}'s registration use case.
 */
public record Email(String value) {

  // Deliberately simple: syntactic sanity check, not full RFC 5322. Real
  // deliverability is verified out-of-band (e.g. a confirmation email),
  // not by this regex.
  private static final Pattern PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

  public Email {
    if (value == null) {
      throw new IllegalArgumentException("email must not be null");
    }
    value = value.strip().toLowerCase();
    if (!PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("not a valid email address: " + value);
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
