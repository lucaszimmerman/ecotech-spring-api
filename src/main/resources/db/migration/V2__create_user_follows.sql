CREATE TABLE user_follows (
    id UUID PRIMARY KEY,

    follower_id UUID NOT NULL,
    followed_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_follows_follower 
       FOREIGN KEY (follower_id)
       REFERENCES users(id)
       ON DELETE CASCADE,

    CONSTRAINT fk_user_follows_followed
       FOREIGN KEY (followed_id)
       REFERENCES users(id)
       ON DELETE CASCADE,

    CONSTRAINT uk_user_follows_follower_followed
         UNIQUE (follower_id, followed_id),

    CONSTRAINT ck_user_follows_not_self
        CHECK (follower_id <> followed_id)
);