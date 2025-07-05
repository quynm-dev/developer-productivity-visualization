CREATE TABLE issues
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    github_url   TEXT         NOT NULL,
    repo_id      BIGINT       NOT NULL,
    milestone_id BIGINT,
    state        VARCHAR(255) NOT NULL,
    title        TEXT         NOT NULL,
    body         TEXT,
    closed_at    DATETIME,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);