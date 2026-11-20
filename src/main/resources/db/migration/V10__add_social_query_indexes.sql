CREATE INDEX idx_posts_user_created_id ON posts (user_id, created_at DESC, id DESC);
CREATE INDEX idx_posts_created_id ON posts (created_at DESC, id DESC);
CREATE INDEX idx_post_likes_post_id ON post_likes (post_id);
CREATE INDEX idx_comments_post_id ON comments (post_id);
