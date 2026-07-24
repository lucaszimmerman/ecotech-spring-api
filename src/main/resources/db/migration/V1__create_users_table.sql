CREATE TABLE users (
    id UUID PRIMARY KEY,

    username VARCHAR(30) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,

    cover_image_url VARCHAR(500),
    profile_image_url VARCHAR(500),
    city VARCHAR(100),
    website VARCHAR(255),
    bio VARCHAR(500),

    active BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);