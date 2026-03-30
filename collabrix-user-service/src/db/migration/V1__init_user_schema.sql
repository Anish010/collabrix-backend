CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY,

    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,

    first_name VARCHAR(100),
    last_name VARCHAR(100),
    country_code VARCHAR(10),
    contact_no VARCHAR(20),
    organization VARCHAR(255),

    avatar_url VARCHAR(500),
    bio TEXT,
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255),
    twitter_url VARCHAR(255),
    website_url VARCHAR(255),

    active BOOLEAN NOT NULL DEFAULT TRUE,
    suspend BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',

    profile_completed BOOLEAN DEFAULT FALSE,
    profile_completion_percentage INT DEFAULT 0,

    last_login_at TIMESTAMP,
    last_logout_at TIMESTAMP,

    login_count INT NOT NULL DEFAULT 0,
    logout_count INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_profiles_username ON user_profiles(username);
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_active ON user_profiles(active);


CREATE TABLE IF NOT EXISTS user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    keycloak_user_id UUID NOT NULL,
    role_name VARCHAR(100) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_role UNIQUE (keycloak_user_id, role_name),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (keycloak_user_id)
        REFERENCES user_profiles(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user_role
    ON user_roles(keycloak_user_id, role_name);


CREATE TABLE IF NOT EXISTS user_activity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    keycloak_user_id UUID NOT NULL,
    activity_type VARCHAR(20) NOT NULL, -- LOGIN / LOGOUT
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_activity_user
    ON user_activity(keycloak_user_id);

CREATE INDEX IF NOT EXISTS idx_user_activity_type
    ON user_activity(activity_type);