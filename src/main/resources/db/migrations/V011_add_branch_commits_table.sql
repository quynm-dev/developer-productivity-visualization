CREATE TABLE branch_commits
(
    id          INT          NOT NULL AUTO_INCREMENT,
    repo_id     BIGINT       NOT NULL,
    branch_name TEXT         NOT NULL,
    commit_hash VARCHAR(255) NOT NULL,
    created_at  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);