/**
 * Chat Delivery bounded context — infrastructure layer. Kafka producer/
 * consumer adapters, STOMP controllers, JPA repositories. Room-membership
 * authorization (SR-5) is enforced here, on every read/list/subscribe path.
 */
package com.kafkaddd.chat.chatdelivery.infrastructure;
