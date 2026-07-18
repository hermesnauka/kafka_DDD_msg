-- Identity bounded context: com.kafkaddd.chat.identity.infrastructure.UserEntity
CREATE TABLE users (
    id                     UUID PRIMARY KEY,
    email                  VARCHAR(255) NOT NULL UNIQUE,
    hashed_password        VARCHAR(255) NOT NULL,
    display_name           VARCHAR(50)  NOT NULL,
    registered_at          TIMESTAMPTZ  NOT NULL,
    failed_login_attempts  INT          NOT NULL DEFAULT 0,
    locked_until           TIMESTAMPTZ
);
