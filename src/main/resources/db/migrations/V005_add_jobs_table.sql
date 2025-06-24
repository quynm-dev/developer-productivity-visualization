CREATE TABLE jobs
(
    id              INT          NOT NULL AUTO_INCREMENT,
    repository_name TEXT         NOT NULL,
    status          VARCHAR(255) NOT NULL,
    description     TEXT,
    last_run_at     datetime,
    failed_count    INT          NOT NULL DEFAULT 0,
    created_at      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);