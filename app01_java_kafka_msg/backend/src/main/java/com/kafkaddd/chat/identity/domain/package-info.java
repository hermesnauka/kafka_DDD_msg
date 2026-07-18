/**
 * Identity bounded context — domain layer. Owns {@link com.kafkaddd.chat.identity.domain.User}
 * (aggregate root), its value objects, and the {@link com.kafkaddd.chat.identity.domain.PasswordHasher}
 * port (PLAN_SSDLC.md §3.1 FR-1, §3.2 SR-1/SR-2).
 *
 * <p>This package's {@code UserId} is intentionally a separate type from
 * {@code com.kafkaddd.chat.chatdelivery.domain.UserId} — Chat Delivery
 * never imports Identity's domain types (or vice versa); each bounded
 * context only ever holds the other's identifiers as opaque values
 * (PLAN_SSDLC.md §9's bounded-context separation). They are not merged
 * into a shared kernel.
 *
 * <p>No Spring, Kafka, or JPA imports belong in this package (NFR-Arch-1).
 * {@link com.kafkaddd.chat.identity.domain.PasswordHasher} is a port: the
 * real BCrypt-backed adapter lives in {@code identity.infrastructure}.
 */
package com.kafkaddd.chat.identity.domain;
