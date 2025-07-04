CREATE TABLE commits
(
    id           BIGINT              NOT NULL AUTO_INCREMENT,
    hash         VARCHAR(255) UNIQUE NOT NULL,
    repo_id      BIGINT              NOT NULL,
    user_id      BIGINT,
    github_url   TEXT                NOT NULL,
    message      TEXT                NOT NULL,
    committed_at datetime            NOT NULL,
    total        INT                 NOT NULL,
    additions    INT                 NOT NULL,
    deletions    INT                 NOT NULL,
    created_at   datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_commits_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);