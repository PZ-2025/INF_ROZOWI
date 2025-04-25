CREATE DATABASE IF NOT EXISTS it_task_management;
USE it_task_management;

-- Wyłączenie kontroli kluczy obcych, aby bezproblemowo usunąć istniejące tabele
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS settings;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS team_members;
DROP TABLE IF EXISTS task_activity;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;

-- Tabela roles
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL,
    permissions JSON
) ENGINE=InnoDB;

-- Tabela groups
CREATE TABLE groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(255) NOT NULL,
    description TEXT
) ENGINE=InnoDB;

-- Tabela users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    password_hint VARCHAR(255),
    role_id INT,
    group_id INT,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_users_group FOREIGN KEY (group_id) REFERENCES groups(id)
) ENGINE=InnoDB;

-- Tabela projects
CREATE TABLE projects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    manager_id INT,
    CONSTRAINT fk_projects_manager FOREIGN KEY (manager_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Tabela teams
CREATE TABLE teams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    project_id INT,
    manager_id INT,
    CONSTRAINT fk_teams_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_teams_manager FOREIGN KEY (manager_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Tabela tasks
CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT,
    assigned_to INT,
    team_id INT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50),
    priority ENUM('LOW', 'MEDIUM', 'HIGH'),
    start_date DATE,
    end_date DATE,
    attachment VARCHAR(255),
    comment TEXT,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_tasks_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id),
    CONSTRAINT fk_tasks_team FOREIGN KEY (team_id) REFERENCES teams(id)
) ENGINE=InnoDB;

-- Tabela task_activity
CREATE TABLE task_activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT,
    user_id INT,
    activity_description TEXT,
    activity_date DATETIME,
    CONSTRAINT fk_task_activity_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT fk_task_activity_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Tabela team_members
CREATE TABLE team_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    team_id INT,
    user_id INT,
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Tabela notifications
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    task_id INT NULL,
    notification_type VARCHAR(50),
    description TEXT,
    date DATETIME,
    is_read BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notifications_task FOREIGN KEY (task_id) REFERENCES tasks(id)
) ENGINE=InnoDB;

-- Tabela settings
CREATE TABLE settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    authentication_method VARCHAR(50),
    data_storage VARCHAR(50),
    language VARCHAR(50),
    theme VARCHAR(50),
    notifications_enabled BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Tabela reports
CREATE TABLE reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(50),
    report_scope JSON,
    created_by INT,
    created_at DATETIME,
    exported_file VARCHAR(255),
    CONSTRAINT fk_reports_user FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;
