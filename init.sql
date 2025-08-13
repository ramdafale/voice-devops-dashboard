-- Voice DevOps Database Initialization Script

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS voice_devops CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE voice_devops;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'USER') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create builds table
CREATE TABLE IF NOT EXISTS builds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    jenkins_build_id VARCHAR(100) UNIQUE,
    branch_name VARCHAR(100) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    status ENUM('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'ABORTED', 'PENDING_APPROVAL') DEFAULT 'PENDING',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    triggered_by BIGINT,
    requires_approval BOOLEAN DEFAULT FALSE,
    api_name VARCHAR(100) NULL,
    build_log TEXT,
    FOREIGN KEY (triggered_by) REFERENCES users(id)
);

-- Create voice_commands table
CREATE TABLE IF NOT EXISTS voice_commands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    command_text TEXT NOT NULL,
    processed_text TEXT,
    intent VARCHAR(100),
    status ENUM('PENDING', 'PROCESSED', 'FAILED') DEFAULT 'PENDING',
    user_id BIGINT,
    build_id BIGINT NULL,
    confidence DECIMAL(3,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (build_id) REFERENCES builds(id)
);

-- Insert default admin user
INSERT INTO users (username, email, password, role) VALUES 
('admin', 'admin@voice-devops.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ADMIN')
ON DUPLICATE KEY UPDATE username=username;

-- Insert default user
INSERT INTO users (username, email, password, role) VALUES 
('developer', 'developer@voice-devops.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'USER')
ON DUPLICATE KEY UPDATE username=username;

-- Insert sample builds
INSERT INTO builds (jenkins_build_id, branch_name, environment, status, triggered_by, requires_approval) VALUES
('BUILD-1001', 'feature/user-authentication', 'staging', 'SUCCESS', 2, FALSE),
('BUILD-1002', 'main', 'production', 'PENDING_APPROVAL', 1, TRUE),
('BUILD-1003', 'feature/voice-commands', 'staging', 'RUNNING', 2, FALSE),
('BUILD-1004', 'release-2.1.0', 'production', 'PENDING_APPROVAL', 1, TRUE);

-- Insert sample voice commands
INSERT INTO voice_commands (command_text, processed_text, intent, status, user_id, confidence) VALUES
('Build my feature branch', 'Build my feature branch', 'BUILD_BRANCH', 'PROCESSED', 2, 0.95),
('Approve build 1002', 'Approve build 1002', 'APPROVE_BUILD', 'PROCESSED', 1, 0.92),
('Deploy to production', 'Deploy to production', 'DEPLOY_PRODUCTION', 'PENDING', 1, 0.88); 