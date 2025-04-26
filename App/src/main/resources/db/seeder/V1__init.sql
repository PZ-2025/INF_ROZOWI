-- Wyłącz sprawdzanie kluczy obcych, aby nie było problemów z kolejnością wstawiania
SET FOREIGN_KEY_CHECKS = 0;

-- Użyj schematu
CREATE SCHEMA IF NOT EXISTS it_task_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE it_task_management;

-- Seed dla tabeli ról
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

-- Seed dla tabeli użytkowników
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

INSERT INTO users (name, last_name, password, email, role_id, stanowisko, password_hint) VALUES
('Dawid', 'Olko', 'Olko2002&', 'dawio@onet.pl', 4, 'Pracownik', 'Ulubione hasło'),
('Anna', 'Nowak', 'Nowak123!', 'anna.nowak@example.com', 2, 'Kierownik', 'Przypomnij hasło'),
('Piotr', 'Smoła', 'SmołaPass!', 'piotr.smola@example.com', 2, 'Kierownik', 'Hasło Piotra'),
('Łukasz', 'Solecki', 'SoleckiPass!', 'lukasz.solecki@example.com', 3, 'Team Leader', 'Twoje hasło'),
('Marta', 'Kowalska', 'KowalskaPass!', 'marta.kowalska@example.com', 3, 'Team Leader', 'Podpowiedź'),
('Jan', 'Kowalczyk', 'JanPass!', 'jan.kowalczyk@example.com', 4, 'Pracownik', 'Przypomnij'),
('Ewa', 'Zielińska', 'EwaPass!', 'ewa.zielinska@example.com', 4, 'Pracownik', 'Hasło'),
('Robert', 'Wiśniewski', 'RobertPass!', 'robert.wisniewski@example.com', 4, 'Pracownik', 'Hasło'),
('Kamil', 'Maj', 'KamilPass!', 'kamil.maj@example.com', 4, 'Pracownik', 'Podpowiedź'),
('Sylwia', 'Nowicka', 'SylwiaPass!', 'sylwia.nowicka@example.com', 4, 'Pracownik', 'Przypomnij'),
('Michał', 'Lewandowski', 'MichalPass!', 'michal.lewandowski@example.com', 1, 'ADMIN', 'Administrator'),
('Karolina', 'Wójcik', 'KarolinaPass!', 'karolina.wojcik@example.com', 4, 'Pracownik', 'Przypomnij hasło'),
('Tomasz', 'Kozłowski', 'TomaszPass!', 'tomasz.kozlowski@example.com', 4, 'Pracownik', 'Twoje hasło');

-- Seed dla tabeli zadań
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

INSERT INTO tasks (name, description, status, start_date, end_date, team, assigned_to) VALUES
('Stworzenie modułu logowania', 'Opracowanie i wdrożenie bezpiecznego modułu logowania', 'COMPLETED', '2025-02-01', '2025-02-10', 'Zespół A', 1),
('Projekt interfejsu użytkownika', 'Przygotowanie szablonu graficznego aplikacji', 'COMPLETED', '2025-02-05', '2025-02-15', 'Zespół B', 4),
('Implementacja modułu raportowania', 'Stworzenie raportów na podstawie danych o zadaniach', 'IN_PROGRESS', '2025-03-01', NULL, 'Zespół C', 7),
('Optymalizacja bazy danych', 'Analiza wydajności i optymalizacja zapytań', 'OPEN', '2025-03-05', NULL, 'Zespół A', 9),
('Testowanie integracyjne', 'Przeprowadzenie testów systemowych i integracyjnych', 'OPEN', '2025-03-10', NULL, 'Zespół B', 10),
('Aktualizacja dokumentacji technicznej', 'Uzupełnienie dokumentacji o nowe moduły', 'COMPLETED', '2025-01-20', '2025-01-30', 'Zespół C', 2),
('Wdrożenie systemu backupu', 'Konfiguracja systemu backupu i odzyskiwania danych', 'IN_PROGRESS', '2025-03-12', NULL, 'Zespół A', 11),
('Rozwój funkcji powiadomień', 'Dodanie mechanizmu powiadomień w aplikacji', 'OPEN', '2025-03-15', NULL, 'Zespół B', 12),
('Integracja z API zewnętrznym', 'Połączenie z usługą zewnętrzną do wysyłki powiadomień', 'OPEN', '2025-03-18', NULL, 'Zespół C', 3),
('Finalizacja projektu', 'Ostateczne poprawki i przygotowanie do wdrożenia', 'OPEN', '2025-03-25', NULL, 'Zespół A', 5);

-- Seed dla tabeli aktywności przy zadaniach
CREATE TABLE IF NOT EXISTS task_activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    activity_description TEXT,
    activity_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO task_activity (task_id, user_id, activity_description) VALUES
(1, 1, 'Rozpoczęcie prac nad modułem logowania'),
(1, 2, 'Weryfikacja projektu logowania'),
(2, 4, 'Przygotowanie koncepcji interfejsu'),
(2, 5, 'Przekazanie projektu do zespołu graficznego'),
(3, 7, 'Rozpoczęcie implementacji raportów'),
(3, 8, 'Testowanie części raportowej'),
(4, 9, 'Analiza wydajności bazy danych'),
(4, 10, 'Wprowadzenie optymalizacji'),
(5, 11, 'Rozpoczęcie testów integracyjnych'),
(6, 2, 'Aktualizacja dokumentacji systemowej'),
(7, 12, 'Konfiguracja systemu backupu'),
(8, 1, 'Prace nad funkcją powiadomień'),
(9, 3, 'Integracja API zewnętrznym'),
(10, 5, 'Ostateczne poprawki i integracja');

-- Seed dla tabeli raportów
CREATE TABLE IF NOT EXISTS reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    report_name VARCHAR(100) NOT NULL,
    report_scope TEXT,
    details TEXT
);

INSERT INTO reports (report_name, report_scope, details) VALUES
('Raport Q1 - Zespół A', 'Okres: 2025-01-01 do 2025-03-31', 'Szczegółowy raport działań zespołu A, postępy, problemy oraz wyniki testów.'),
('Raport Q1 - Zespół B', 'Okres: 2025-01-01 do 2025-03-31', 'Analiza efektywności, wyniki testów integracyjnych i rekomendacje usprawnień.'),
('Raport Q1 - Zespół C', 'Okres: 2025-01-01 do 2025-03-31', 'Przegląd wdrożonych rozwiązań, integracji oraz funkcjonalności powiadomień.');

-- Włącz ponownie sprawdzanie kluczy obcych
SET FOREIGN_KEY_CHECKS = 1;
