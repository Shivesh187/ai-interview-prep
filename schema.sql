-- MySQL Database Schema for AI Interview Prep App
CREATE DATABASE IF NOT EXISTS ai_interview_db;
USE ai_interview_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Interviews Table
CREATE TABLE IF NOT EXISTS interviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    job_role VARCHAR(100) NOT NULL,
    difficulty VARCHAR(50) NOT NULL, -- e.g., Entry, Mid, Senior
    interview_type VARCHAR(50) NOT NULL, -- e.g., Technical, Behavioral
    overall_score DECIMAL(4, 2) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Interview Questions & Evaluation Table
CREATE TABLE IF NOT EXISTS interview_questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    interview_id INT NOT NULL,
    question_text TEXT NOT NULL,
    user_answer TEXT DEFAULT NULL,
    ai_feedback TEXT DEFAULT NULL,
    score INT DEFAULT NULL, -- Score from 1-10
    model_answer TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (interview_id) REFERENCES interviews(id) ON DELETE CASCADE
) ENGINE=InnoDB;
