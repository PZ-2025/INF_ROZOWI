-- Wstawienie danych do tabeli roles
INSERT INTO roles (role_name, permissions) VALUES
('Administrator', '{"zarzadzanie_uzytkownikami": true, "konfiguracja_systemu": true}'),
('Kierownik', '{"przydzielanie_zadan": true, "generowanie_raportow": true}'),
('Użytkownik', '{"przeglad_zadan": true, "aktualizacja_zadan": true}');

-- Wstawienie danych do tabeli groups
INSERT INTO groups (group_name, description) VALUES
('Deweloperzy', 'Grupa odpowiedzialna za tworzenie oprogramowania.'),
('Testerzy', 'Grupa odpowiedzialna za testowanie aplikacji.'),
('Analitycy', 'Grupa analizująca wymagania i dane.');

-- Wstawienie danych do tabeli users
INSERT INTO users (name, last_name, email, password, password_hint, role_id, group_id) VALUES
('Jan', 'Kowalski', 'jan.kowalski@firma.pl', 'pass123', 'ulubione imie zwierząt', 1, 1),
('Anna', 'Nowak', 'anna.nowak@firma.pl', 'pass234', 'pierwsza litera nazwiska', 2, 1),
('Piotr', 'Wiśniewski', 'piotr.wisniewski@firma.pl', 'pass345', 'ulubiony kolor', 3, 2),
('Katarzyna', 'Zielińska', 'katarzyna.zielinska@firma.pl', 'pass456', 'data urodzenia', 3, 3),
('Marek', 'Lewandowski', 'marek.lewandowski@firma.pl', 'pass567', 'numer telefonu', 3, 1);

-- Wstawienie danych do tabeli projects
INSERT INTO projects (project_name, description, start_date, end_date, manager_id) VALUES
('System zarządzania zadaniami', 'Projekt systemu do zarządzania zadaniami w firmie IT.', '2023-01-01', '2023-12-31', 2),
('Aplikacja mobilna', 'Projekt mobilnej aplikacji do współpracy i komunikacji.', '2023-03-01', '2023-11-30', 2);

-- Wstawienie danych do tabeli teams
INSERT INTO teams (team_name, project_id, manager_id) VALUES
('Zespół A', 1, 2),
('Zespół B', 2, 2);

-- Wstawienie danych do tabeli tasks
INSERT INTO tasks (project_id, assigned_to, team_id, name, description, status, priority, start_date, end_date, attachment, comment) VALUES
(1, 3, 1, 'Implementacja logiki', 'Implementacja głównej logiki systemu zarządzania zadaniami.', 'w toku', 'HIGH', '2023-01-15', '2023-02-15', 'sciezka/do/pliku.pdf', 'Początkowy etap implementacji'),
(1, 4, 1, 'Testowanie funkcji', 'Testowanie modułu logowania i rejestracji.', 'oczekuje', 'MEDIUM', '2023-02-01', '2023-02-28', NULL, 'Planowane testy po wdrożeniu'),
(2, 5, 2, 'Projekt UI', 'Przygotowanie projektu graficznego interfejsu użytkownika.', 'zakończone', 'LOW', '2023-03-10', '2023-04-10', 'sciezka/do/ui.pdf', 'Zatwierdzony projekt przez kierownika');

-- Wstawienie danych do tabeli task_activity
INSERT INTO task_activity (task_id, user_id, activity_description, activity_date) VALUES
(1, 3, 'Rozpoczęcie implementacji', '2023-01-15 09:00:00'),
(2, 4, 'Przygotowanie środowiska testowego', '2023-02-01 10:00:00'),
(3, 5, 'Przekazanie projektu UI do wdrożenia', '2023-03-10 11:00:00');

-- Wstawienie danych do tabeli team_members
INSERT INTO team_members (team_id, user_id) VALUES
(1, 3),
(1, 4),
(2, 5);

-- Wstawienie danych do tabeli notifications
INSERT INTO notifications (user_id, task_id, notification_type, description, date, is_read) VALUES
(3, 1, 'przypisanie', 'Zostałeś przypisany do zadania "Implementacja logiki".', '2023-01-15 09:05:00', FALSE),
(4, NULL, 'system', 'Aktualizacja systemu w dniu 2023-02-01', '2023-02-01 08:00:00', FALSE);

-- Wstawienie danych do tabeli settings
INSERT INTO settings (user_id, authentication_method, data_storage, language, theme, notifications_enabled) VALUES
(1, 'hasło', 'lokalne', 'polski', 'jasny', TRUE),
(2, 'hasło', 'chmurowe', 'polski', 'ciemny', TRUE),
(3, 'hasło', 'lokalne', 'polski', 'jasny', TRUE);

-- Wstawienie danych do tabeli reports
INSERT INTO reports (report_name, report_type, report_scope, created_by, created_at, exported_file) VALUES
('Raport obciążenia pracowników', 'workload', '{"pracownicy": ["Piotr", "Katarzyna"]}', 2, '2023-02-05 10:00:00', 'raport1.pdf'),
('Raport postępu projektu', 'project_progress', '{"projekt": "System zarządzania zadaniami"}', 2, '2023-03-15 15:00:00', 'raport2.pdf');
