CREATE TABLE repositories
(
    id           BIGINT              NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255) UNIQUE NOT NULL,
    github_url   TEXT                NOT NULL,
    user_id      BIGINT              NOT NULL,
    language     VARCHAR(255)        NOT NULL,
    pulls_url    TEXT                NOT NULL,
    commits_url  TEXT                NOT NULL,
    last_sync_at datetime,
    created_at   datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_repositories_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);