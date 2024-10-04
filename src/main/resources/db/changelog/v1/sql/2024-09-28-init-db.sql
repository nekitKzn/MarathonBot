CREATE TABLE "users"
(
    "telegram_id"            BIGINT                         NOT NULL PRIMARY KEY,
    "created_at"             TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at"             TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "admin"                  BOOLEAN                        NOT NULL DEFAULT false,
    "telegram_user_name"     VARCHAR(255)                   NULL,
    "telegram_first_name"    VARCHAR(255)                   NULL,
    "state"                  VARCHAR(255)                   NOT NULL,
    "count_change_state"     BIGINT                         NOT NULL DEFAULT 0,
    "count_change_state_all" BIGINT                         NOT NULL DEFAULT 0,
    "is_playing"             BOOLEAN                        NOT NULL DEFAULT false
);

CREATE TABLE "goal"
(
    "id"       BIGINT       NOT NULL PRIMARY KEY,
    "user_id"  BIGINT       NOT NULL REFERENCES "users" ("telegram_id"),
    "name"     VARCHAR(255) NOT NULL,
    "position" BIGINT       NOT NULL
);

CREATE TABLE "history"
(
    "id"         SERIAL                         NOT NULL PRIMARY KEY,
    "created_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "updated_at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    "goal_id"    BIGINT                         NOT NULL REFERENCES "goal" ("id"),
    "done"       BOOLEAN                        NULL
);