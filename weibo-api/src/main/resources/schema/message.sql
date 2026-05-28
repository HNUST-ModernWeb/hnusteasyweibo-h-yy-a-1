CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    from_nickname VARCHAR(100) NOT NULL,
    from_avatar VARCHAR(255),
    type VARCHAR(20) NOT NULL COMMENT 'like, comment, reply, follow',
    post_id BIGINT,
    comment_id BIGINT,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_user_id ON message(user_id);
CREATE INDEX idx_message_is_read ON message(is_read);
CREATE INDEX idx_message_create_time ON message(create_time);