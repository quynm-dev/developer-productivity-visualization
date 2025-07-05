CREATE TABLE releases
(
    id                  BIGINT   NOT NULL AUTO_INCREMENT,
    github_url          TEXT     NOT NULL,
    repo_id             BIGINT   NOT NULL,
    tag_name            TEXT     NOT NULL,
    branch              TEXT     NOT NULL,
    name                TEXT     NOT NULL,
    body                TEXT     NOT NULL,
    draft               bool     NOT NULL,
    github_created_at   datetime NOT NULL,
    github_published_at datetime NOT NULL,
    created_at          datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);