CREATE TABLE post_likes (
    id UUID PRIMARY KEY,

    post_id UUID NOT NULL,
    user_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_post_likes_user
       FOREIGN KEY (user_id)
       REFERENCES users(id)
       ON DELETE CASCADE,

    CONSTRAINT fk_post_likes_post
       FOREIGN KEY (post_id)
       REFERENCES posts(id)
       ON DELETE CASCADE,

    CONSTRAINT uk_post_likes_user_post
          UNIQUE (user_id, post_id)

)