ALTER TABLE comment ADD COLUMN parent_id BIGINT NULL;
ALTER TABLE comment ADD COLUMN reply_to_nickname VARCHAR(100) NULL;
ALTER TABLE comment ADD COLUMN reply_to_user_id BIGINT NULL;