CREATE TABLE posts (
    id UUID PRIMARY KEY,

    content TEXT NOT NULL,
    image_url VARCHAR(500),

    user_id UUID NOT NULL,


    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_posts_user
       FOREIGN KEY (user_id)
       REFERENCES users(id)
       ON DELETE CASCADE

);