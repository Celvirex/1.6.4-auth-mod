-- Create database and user
CREATE DATABASE IF NOT EXISTS minecraft_auth;
CREATE USER IF NOT EXISTS 'minecraft'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON minecraft_auth.* TO 'minecraft'@'localhost';
FLUSH PRIVILEGES;

-- Use the database
USE minecraft_auth;

-- Create players table
CREATE TABLE IF NOT EXISTS players (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_online BOOLEAN DEFAULT FALSE,
    INDEX idx_username (username),
    INDEX idx_online (is_online)
);

-- Create sessions table (optional, for advanced features)
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(64) PRIMARY KEY,
    player_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    INDEX idx_expires (expires_at)
);
