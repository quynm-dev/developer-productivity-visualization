CREATE TABLE branches
(
    id         INT   NOT NULL AUTO_INCREMENT,
    name       text     NOT NULL,
    repo_id    BIGINT   NOT NULL,
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);