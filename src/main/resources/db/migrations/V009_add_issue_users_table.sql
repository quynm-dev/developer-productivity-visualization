CREATE TABLE issue_users
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    issue_id BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_issue_users_issue_id__id` FOREIGN KEY (`issue_id`) REFERENCES `issues` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_issue_users_user_id__id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
); 