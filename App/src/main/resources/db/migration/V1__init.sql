-- Utworzenie schematu, jeśli nie istnieje
CREATE SCHEMA IF NOT EXISTS it_task_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE it_task_management;

-- Tabela ról
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (role_name) VALUES
    ('ADMIN'),
    ('KIEROWNIK'),
    ('TEAM_LEADER'),
    ('PRACOWNIK')
ON DUPLICATE KEY UPDATE role_name = role_name;

-- Tabela użytkowników
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role_id INT NOT NULL,
    stanowisko VARCHAR(50),
    password_hint TEXT,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Tabela zadań
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'OPEN',
    start_date DATE,
    end_date DATE,
    team VARCHAR(100),
    assigned_to INT,
    FOREIGN KEY (assigned_to) REFERENCES users(id)
);

-- Tabela aktywności przy zadaniach
CREATE TABLE IF NOT EXISTS task_activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    activity_description TEXT,
    activity_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tabela raportów
CREATE TABLE IF NOT EXISTS reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    report_name VARCHAR(100) NOT NULL,
    report_scope TEXT,
    details TEXT
);
