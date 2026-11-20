ALTER TABLE users
    ALTER COLUMN password DROP NOT NULL;

ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE users
    ADD COLUMN provider_id VARCHAR(255);

ALTER TABLE users
    ADD CONSTRAINT uk_users_auth_provider_provider_id
        UNIQUE (auth_provider, provider_id);