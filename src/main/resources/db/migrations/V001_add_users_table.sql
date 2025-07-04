CREATE TABLE users
(
    id         BIGINT              NOT NULL AUTO_INCREMENT,
    username   VARCHAR(255) UNIQUE NOT NULL,
    avatar_url TEXT                NOT NULL,
    github_url TEXT                NOT NULL,
    created_at datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);