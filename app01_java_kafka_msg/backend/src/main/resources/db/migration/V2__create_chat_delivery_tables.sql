-- Chat Delivery bounded context: com.kafkaddd.chat.chatdelivery.infrastructure.*
CREATE TABLE chat_rooms (
    id  UUID PRIMARY KEY
);

CREATE TABLE chat_room_participants (
    id        UUID PRIMARY KEY,
    room_id   UUID NOT NULL REFERENCES chat_rooms(id),
    user_id   UUID NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    UNIQUE (room_id, user_id)
);

CREATE INDEX idx_chat_room_participants_user_id ON chat_room_participants(user_id);

CREATE TABLE chat_messages (
    id        UUID PRIMARY KEY,
    room_id   UUID NOT NULL REFERENCES chat_rooms(id),
    sender_id UUID NOT NULL,
    content   TEXT NOT NULL,
    sent_at   TIMESTAMPTZ NOT NULL,
    status    VARCHAR(20) NOT NULL
);

CREATE INDEX idx_chat_messages_room_id ON chat_messages(room_id);
