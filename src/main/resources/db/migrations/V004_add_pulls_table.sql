CREATE TABLE pulls
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    github_url        TEXT         NOT NULL,
    state             VARCHAR(255) NOT NULL,
    title             TEXT         NOT NULL,
    user_id           BIGINT       NOT NULL,
    repo_id           BIGINT       NOT NULL,
    closed_at         datetime,
    merged_at         datetime,
    github_created_at datetime     NOT NULL,
    github_updated_at datetime     NOT NULL,
    created_at        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_pulls_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);