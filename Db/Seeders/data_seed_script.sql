-- Wstawianie ról (bez kolumny permissions)
INSERT INTO roles (id, role_name) VALUES
  (1, 'Administrator'),
  (2, 'Kierownik'),
  (3, 'Team Lider'),
  (4, 'Pracownik');

INSERT INTO groups (id, group_name) VALUES
  (1, 'Developerzy'),
  (2, 'Testerzy'),
  (3, 'Projektanci'),
  (4, 'Analitycy'),
  (5, 'Menedżerowie'),
  (6, 'DevOps'),
  (7, 'QA'),
  (8, 'Business Analyst'),
  (9, 'Wsparcie'),
  (10, 'HR');

-- Hasło to: Haslo#12345
-- Administratorzy (3)
INSERT INTO users (id, name, last_name, email, password, password_hint, role_id, group_id) VALUES
  (1, 'Jan', 'Kowalski', 'jan.kowalski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz1', 3, 5),
  (2, 'Anna', 'Nowak', 'anna.nowak@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz2', 3, 5),
  (3, 'Piotr', 'Wiśniewski', 'piotr.wisniewski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz3', 3, 5);

-- Kierownicy (3)
INSERT INTO users (id, name, last_name, email, password, password_hint, role_id, group_id) VALUES
  (4, 'Katarzyna', 'Zielińska', 'katarzyna.zielinska@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz4', 2, 5),
  (5, 'Tomasz', 'Wójcik', 'tomasz.wojcik@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz5', 2, 5),
  (6, 'Agnieszka', 'Krawczyk', 'agnieszka.krawczyk@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz6', 2, 5);

-- Team Liderzy (10)
INSERT INTO users (id, name, last_name, email, password, password_hint, role_id, group_id) VALUES
  (7, 'Michał', 'Lewandowski', 'michal.lewandowski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz7', 1, 1),
  (8, 'Monika', 'Szymańska', 'monika.szymansk@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz8', 1, 2),
  (9, 'Paweł', 'Dąbrowski', 'pawel.dabrowski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz9', 1, 1),
  (10, 'Ewa', 'Kozłowska', 'ewa.kozlowska@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz10', 1, 3),
  (11, 'Krzysztof', 'Jankowski', 'krzysztof.jankowski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz11', 1, 2),
  (12, 'Barbara', 'Wojciechowska', 'barbara.wojciechowska@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz12', 1, 3),
  (13, 'Adam', 'Kubiak', 'adam.kubiak@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz13', 1, 1),
  (14, 'Elżbieta', 'Wróblewska', 'elzbieta.wroblewska@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz14', 1, 2),
  (15, 'Robert', 'Nowicki', 'robert.nowicki@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz15', 1, 3),
  (16, 'Iwona', 'Mazur', 'iwona.mazur@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz16', 1, 1);

-- Pracownicy (24)
INSERT INTO users (id, name, last_name, email, password, password_hint, role_id, group_id) VALUES
  (17, 'Łukasz', 'Grabowski', 'lukasz.grabowski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz17', 4, 1),
  (18, 'Karolina', 'Nowakowska', 'karolina.nowakowska@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz18', 4, 2),
  (19, 'Sebastian', 'Walczak', 'sebastian.walczak@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz19', 4, 3),
  (20, 'Julia', 'Baran', 'julia.baran@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz20', 4, 4),
  (21, 'Damian', 'Sikorski', 'damian.sikorski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz21', 4, 6),
  (22, 'Natalia', 'Pawłowska', 'natalia.pawlowska@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz22', 4, 7),
  (23, 'Marek', 'Zawadzki', 'marek.zawadzki@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz23', 4, 8),
  (24, 'Dorota', 'Piotrowska', 'dorota.piotrowska@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz24', 4, 9),
  (25, 'Szymon', 'Król', 'szymon.krol@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz25', 4, 10),
  (26, 'Weronika', 'Majewska', 'weronika.majewska@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz26', 4, 1),
  (27, 'Oskar', 'Górski', 'oskar.gorski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz27', 4, 2),
  (28, 'Renata', 'Walczak', 'renata.walczak@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz28', 4, 3),
  (29, 'Adrian', 'Stępień', 'adrian.stepien@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz29', 4, 4),
  (30, 'Klaudia', 'Michalska', 'klaudia.michalska@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz30', 4, 6),
  (31, 'Igor', 'Lis', 'igor.lis@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz31', 4, 7),
  (32, 'Agata', 'Wójcik', 'agata.wojcik@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz32', 4, 8),
  (33, 'Filip', 'Brzeziński', 'filip.brzezinski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz33', 4, 9),
  (34, 'Martyna', 'Nowak', 'martyna.nowak@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz34', 4, 10),
  (35, 'Konstanty', 'Ostrowski', 'konstanty.ostrowski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz35', 4, 1),
  (36, 'Emilia', 'Adamczyk', 'emilia.adamczyk@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz36', 4, 2),
  (37, 'Dawid', 'Jasiński', 'dawid.jasinski@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz37', 4, 3),
  (38, 'Irek', 'Barczak', 'irek.barczak@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz38', 4, 4),
  (39, 'Julia', 'Kowalczyk', 'julia.kowalczyk@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz39', 4, 6),
  (40, 'Sylwia', 'Głowacka', 'sylwia.glowacka@firma.pl', '95b49eab894a32254b0990346390a167a45ec751e5e72da9f45abae5f7d9340d', 'podpowiedz40', 4, 7);

INSERT INTO settings (user_id, theme, default_view, last_password_change) VALUES
  (1, 'light', 'moje_zadania', CURRENT_TIMESTAMP()),
  (2, 'dark', 'zadania', CURRENT_TIMESTAMP()),
  (3, 'light', 'moje_zadania', CURRENT_TIMESTAMP()),
  (4, 'dark', 'zadania', CURRENT_TIMESTAMP()),
  (5, 'light', 'moje_zadania', CURRENT_TIMESTAMP()),
  (6, 'dark', 'zadania', CURRENT_TIMESTAMP()),
  (7, 'light', 'moje_zadania', CURRENT_TIMESTAMP()),
  (8, 'dark', 'zadania', CURRENT_TIMESTAMP()),
  (9, 'light', 'moje_zadania', CURRENT_TIMESTAMP()),
  (10, 'dark', 'zadania', CURRENT_TIMESTAMP());

-- Najpierw wstawiamy zespoły, ponieważ projekty będą się do nich odnosić
INSERT INTO teams (id, team_name) VALUES
  (1, 'Zespół Alfa'),
  (2, 'Zespół Beta'),
  (3, 'Zespół Gamma'),
  (4, 'Zespół Delta'),
  (5, 'Zespół Epsilon'),
  (6, 'Zespół Zeta'),
  (7, 'Zespół Eta'),
  (8, 'Zespół Theta'),
  (9, 'Zespół Iota'),
  (10, 'Zespół Downa');

-- Teraz wstawiamy projekty z odniesieniem do zespołów
INSERT INTO projects (id, project_name, description, start_date, end_date, manager_id, team_id) VALUES
  (1, 'Projekt 1', 'Opis projektu 1', '2025-05-01', '2025-07-01', 4, 1),
  (2, 'Projekt 2', 'Opis projektu 2', '2025-05-05', '2025-07-05', 5, 2),
  (3, 'Projekt 3', 'Opis projektu 3', '2025-05-10', '2025-07-10', 6, 3),
  (4, 'Projekt 4', 'Opis projektu 4', '2025-05-15', '2025-07-15', 4, 4),
  (5, 'Projekt 5', 'Opis projektu 5', '2025-05-20', '2025-07-20', 5, 5),
  (6, 'Projekt 6', 'Opis projektu 6', '2025-05-25', '2025-07-25', 6, 6),
  (7, 'Projekt 7', 'Opis projektu 7', '2025-06-01', '2025-08-01', 4, 7),
  (8, 'Projekt 8', 'Opis projektu 8', '2025-06-05', '2025-08-05', 5, 8),
  (9, 'Projekt 9', 'Opis projektu 9', '2025-06-10', '2025-08-10', 6, 9),
  (10, 'Projekt 10', 'Opis projektu 10', '2025-06-15', '2025-08-15', 4, 10),
  (11, 'Projekt 11', 'Opis projektu 11', '2025-06-20', '2025-08-20', 5, 1),
  (12, 'Projekt 12', 'Opis projektu 12', '2025-06-25', '2025-08-25', 6, 2),
  (13, 'Projekt 13', 'Opis projektu 13', '2025-07-01', '2025-09-01', 4, 3),
  (14, 'Projekt 14', 'Opis projektu 14', '2025-07-05', '2025-09-05', 5, 4),
  (15, 'Projekt 15', 'Opis projektu 15', '2025-07-10', '2025-09-10', 6, 5),
  (16, 'Projekt 16', 'Opis projektu 16', '2025-07-15', '2025-09-15', 4, 6),
  (17, 'Projekt 17', 'Opis projektu 17', '2025-07-20', '2025-09-20', 5, 7),
  (18, 'Projekt 18', 'Opis projektu 18', '2025-07-25', '2025-09-25', 6, 8),
  (19, 'Projekt 19', 'Opis projektu 19', '2025-08-01', '2025-10-01', 4, 9),
  (20, 'Projekt 20', 'Opis projektu 20', '2025-08-05', '2025-10-05', 5, 10);

-- Każdy team ma przypisanego team lidera (użytkownicy id 7-16) oraz kilku pracowników
-- Team 1: lider: id=7, członkowie: 17, 18, 37
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (1, 7, 1),
  (1, 17, 0),
  (1, 18, 0),
  (1, 37, 0);
  
-- Team 2: lider: id=8, członkowie: 19, 20, 38
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (2, 8, 1),
  (2, 19, 0),
  (2, 20, 0),
  (2, 38, 0);
  
-- Team 3: lider: id=9, członkowie: 21, 22, 39
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (3, 9, 1),
  (3, 21, 0),
  (3, 22, 0),
  (3, 39, 0);
  
-- Team 4: lider: id=10, członkowie: 23, 24, 40
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (4, 10, 1),
  (4, 23, 0),
  (4, 24, 0),
  (4, 40, 0);
  
-- Team 5: lider: id=11, członkowie: 25, 26
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (5, 11, 1),
  (5, 25, 0),
  (5, 26, 0);
  
-- Team 6: lider: id=12, członkowie: 27, 28
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (6, 12, 1),
  (6, 27, 0),
  (6, 28, 0);
  
-- Team 7: lider: id=13, członkowie: 29, 30
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (7, 13, 1),
  (7, 29, 0),
  (7, 30, 0);
  
-- Team 8: lider: id=14, członkowie: 31, 32
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (8, 14, 1),
  (8, 31, 0),
  (8, 32, 0);
  
-- Team 9: lider: id=15, członkowie: 33, 34
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (9, 15, 1),
  (9, 33, 0),
  (9, 34, 0);
  
-- Team 10: lider: id=16, członkowie: 35, 36
INSERT INTO team_members (team_id, user_id, is_leader) VALUES
  (10, 16, 1),
  (10, 35, 0),
  (10, 36, 0);

-- Każde zadanie powiązane jest z projektem (project_id) oraz teamem (team_id)
-- Używamy cyklicznego przypisania: project_id = (id_zadania mod 20)+1, team_id = (id_zadania mod 10)+1
INSERT INTO tasks (id, project_id, team_id, title, description, status, priority, start_date, end_date) VALUES
  (1, ((1 % 20) + 1), ((1 % 10) + 1), 'Zadanie 1', 'Opis zadania 1', 'Nowe', 'MEDIUM', '2025-05-01', '2025-05-15'),
  (2, ((2 % 20) + 1), ((2 % 10) + 1), 'Zadanie 2', 'Opis zadania 2', 'W toku', 'MEDIUM', '2025-05-02', '2025-05-16'),
  (3, ((3 % 20) + 1), ((3 % 10) + 1), 'Zadanie 3', 'Opis zadania 3', 'Zakończone', 'MEDIUM', '2025-05-03', '2025-05-17'),
  (4, ((4 % 20) + 1), ((4 % 10) + 1), 'Zadanie 4', 'Opis zadania 4', 'Nowe', 'MEDIUM', '2025-05-04', '2025-05-18'),
  (5, ((5 % 20) + 1), ((5 % 10) + 1), 'Zadanie 5', 'Opis zadania 5', 'W toku', 'MEDIUM', '2025-05-05', '2025-05-19'),
  (6, ((6 % 20) + 1), ((6 % 10) + 1), 'Zadanie 6', 'Opis zadania 6', 'Zakończone', 'MEDIUM', '2025-05-06', '2025-05-20'),
  (7, ((7 % 20) + 1), ((7 % 10) + 1), 'Zadanie 7', 'Opis zadania 7', 'Nowe', 'MEDIUM', '2025-05-07', '2025-05-21'),
  (8, ((8 % 20) + 1), ((8 % 10) + 1), 'Zadanie 8', 'Opis zadania 8', 'W toku', 'MEDIUM', '2025-05-08', '2025-05-22'),
  (9, ((9 % 20) + 1), ((9 % 10) + 1), 'Zadanie 9', 'Opis zadania 9', 'Zakończone', 'MEDIUM', '2025-05-09', '2025-05-23'),
  (10, ((10 % 20) + 1), ((10 % 10) + 1), 'Zadanie 10', 'Opis zadania 10', 'Nowe', 'MEDIUM', '2025-05-10', '2025-05-24'),
  (11, ((11 % 20) + 1), ((11 % 10) + 1), 'Zadanie 11', 'Opis zadania 11', 'W toku', 'MEDIUM', '2025-05-11', '2025-05-25'),
  (12, ((12 % 20) + 1), ((12 % 10) + 1), 'Zadanie 12', 'Opis zadania 12', 'Zakończone', 'MEDIUM', '2025-05-12', '2025-05-26'),
  (13, ((13 % 20) + 1), ((13 % 10) + 1), 'Zadanie 13', 'Opis zadania 13', 'Nowe', 'MEDIUM', '2025-05-13', '2025-05-27'),
  (14, ((14 % 20) + 1), ((14 % 10) + 1), 'Zadanie 14', 'Opis zadania 14', 'W toku', 'MEDIUM', '2025-05-14', '2025-05-28'),
  (15, ((15 % 20) + 1), ((15 % 10) + 1), 'Zadanie 15', 'Opis zadania 15', 'Zakończone', 'MEDIUM', '2025-05-15', '2025-05-29'),
  (16, ((16 % 20) + 1), ((16 % 10) + 1), 'Zadanie 16', 'Opis zadania 16', 'Nowe', 'MEDIUM', '2025-05-16', '2025-05-30'),
  (17, ((17 % 20) + 1), ((17 % 10) + 1), 'Zadanie 17', 'Opis zadania 17', 'W toku', 'MEDIUM', '2025-05-17', '2025-05-31'),
  (18, ((18 % 20) + 1), ((18 % 10) + 1), 'Zadanie 18', 'Opis zadania 18', 'Zakończone', 'MEDIUM', '2025-05-18', '2025-06-01'),
  (19, ((19 % 20) + 1), ((19 % 10) + 1), 'Zadanie 19', 'Opis zadania 19', 'Nowe', 'MEDIUM', '2025-05-19', '2025-06-02'),
  (20, ((20 % 20) + 1), ((20 % 10) + 1), 'Zadanie 20', 'Opis zadania 20', 'W toku', 'MEDIUM', '2025-05-20', '2025-06-03'),
  (21, ((21 % 20) + 1), ((21 % 10) + 1), 'Zadanie 21', 'Opis zadania 21', 'Zakończone', 'MEDIUM', '2025-05-21', '2025-06-04'),
  (22, ((22 % 20) + 1), ((22 % 10) + 1), 'Zadanie 22', 'Opis zadania 22', 'Nowe', 'MEDIUM', '2025-05-22', '2025-06-05'),
  (23, ((23 % 20) + 1), ((23 % 10) + 1), 'Zadanie 23', 'Opis zadania 23', 'W toku', 'MEDIUM', '2025-05-23', '2025-06-06'),
  (24, ((24 % 20) + 1), ((24 % 10) + 1), 'Zadanie 24', 'Opis zadania 24', 'Zakończone', 'MEDIUM', '2025-05-24', '2025-06-07'),
  (25, ((25 % 20) + 1), ((25 % 10) + 1), 'Zadanie 25', 'Opis zadania 25', 'Nowe', 'MEDIUM', '2025-05-25', '2025-06-08'),
  (26, ((26 % 20) + 1), ((26 % 10) + 1), 'Zadanie 26', 'Opis zadania 26', 'W toku', 'MEDIUM', '2025-05-26', '2025-06-09'),
  (27, ((27 % 20) + 1), ((27 % 10) + 1), 'Zadanie 27', 'Opis zadania 27', 'Zakończone', 'MEDIUM', '2025-05-27', '2025-06-10'),
(28, ((28 % 20) + 1), ((28 % 10) + 1), 'Zadanie 28', 'Opis zadania 28', 'Nowe', 'MEDIUM', '2025-05-28', '2025-06-11'),
  (29, ((29 % 20) + 1), ((29 % 10) + 1), 'Zadanie 29', 'Opis zadania 29', 'W toku', 'MEDIUM', '2025-05-29', '2025-06-12'),
  (30, ((30 % 20) + 1), ((30 % 10) + 1), 'Zadanie 30', 'Opis zadania 30', 'Zakończone', 'MEDIUM', '2025-05-30', '2025-06-13'),
  (31, ((31 % 20) + 1), ((31 % 10) + 1), 'Zadanie 31', 'Opis zadania 31', 'Nowe', 'MEDIUM', '2025-05-31', '2025-06-14'),
  (32, ((32 % 20) + 1), ((32 % 10) + 1), 'Zadanie 32', 'Opis zadania 32', 'W toku', 'MEDIUM', '2025-06-01', '2025-06-15'),
  (33, ((33 % 20) + 1), ((33 % 10) + 1), 'Zadanie 33', 'Opis zadania 33', 'Zakończone', 'MEDIUM', '2025-06-02', '2025-06-16'),
  (34, ((34 % 20) + 1), ((34 % 10) + 1), 'Zadanie 34', 'Opis zadania 34', 'Nowe', 'MEDIUM', '2025-06-03', '2025-06-17'),
  (35, ((35 % 20) + 1), ((35 % 10) + 1), 'Zadanie 35', 'Opis zadania 35', 'W toku', 'MEDIUM', '2025-06-04', '2025-06-18'),
  (36, ((36 % 20) + 1), ((36 % 10) + 1), 'Zadanie 36', 'Opis zadania 36', 'Zakończone', 'MEDIUM', '2025-06-05', '2025-06-19'),
  (37, ((37 % 20) + 1), ((37 % 10) + 1), 'Zadanie 37', 'Opis zadania 37', 'Nowe', 'MEDIUM', '2025-06-06', '2025-06-20'),
  (38, ((38 % 20) + 1), ((38 % 10) + 1), 'Zadanie 38', 'Opis zadania 38', 'W toku', 'MEDIUM', '2025-06-07', '2025-06-21'),
  (39, ((39 % 20) + 1), ((39 % 10) + 1), 'Zadanie 39', 'Opis zadania 39', 'Zakończone', 'MEDIUM', '2025-06-08', '2025-06-22'),
  (40, ((40 % 20) + 1), ((40 % 10) + 1), 'Zadanie 40', 'Opis zadania 40', 'Nowe', 'MEDIUM', '2025-06-09', '2025-06-23'),
  (41, ((41 % 20) + 1), ((41 % 10) + 1), 'Zadanie 41', 'Opis zadania 41', 'W toku', 'MEDIUM', '2025-06-10', '2025-06-24'),
  (42, ((42 % 20) + 1), ((42 % 10) + 1), 'Zadanie 42', 'Opis zadania 42', 'Zakończone', 'MEDIUM', '2025-06-11', '2025-06-25'),
  (43, ((43 % 20) + 1), ((43 % 10) + 1), 'Zadanie 43', 'Opis zadania 43', 'Nowe', 'MEDIUM', '2025-06-12', '2025-06-26'),
  (44, ((44 % 20) + 1), ((44 % 10) + 1), 'Zadanie 44', 'Opis zadania 44', 'W toku', 'MEDIUM', '2025-06-13', '2025-06-27'),
  (45, ((45 % 20) + 1), ((45 % 10) + 1), 'Zadanie 45', 'Opis zadania 45', 'Zakończone', 'MEDIUM', '2025-06-14', '2025-06-28'),
  (46, ((46 % 20) + 1), ((46 % 10) + 1), 'Zadanie 46', 'Opis zadania 46', 'Nowe', 'MEDIUM', '2025-06-15', '2025-06-29'),
  (47, ((47 % 20) + 1), ((47 % 10) + 1), 'Zadanie 47', 'Opis zadania 47', 'W toku', 'MEDIUM', '2025-06-16', '2025-06-30'),
  (48, ((48 % 20) + 1), ((48 % 10) + 1), 'Zadanie 48', 'Opis zadania 48', 'Zakończone', 'MEDIUM', '2025-06-17', '2025-07-01'),
  (49, ((49 % 20) + 1), ((49 % 10) + 1), 'Zadanie 49', 'Opis zadania 49', 'Nowe', 'MEDIUM', '2025-06-18', '2025-07-02'),
  (50, ((50 % 20) + 1), ((50 % 10) + 1), 'Zadanie 50', 'Opis zadania 50', 'W toku', 'MEDIUM', '2025-06-19', '2025-07-03'),
  (51, ((51 % 20) + 1), ((51 % 10) + 1), 'Zadanie 51', 'Opis zadania 51', 'Zakończone', 'MEDIUM', '2025-06-20', '2025-07-04'),
  (52, ((52 % 20) + 1), ((52 % 10) + 1), 'Zadanie 52', 'Opis zadania 52', 'Nowe', 'MEDIUM', '2025-06-21', '2025-07-05'),
  (53, ((53 % 20) + 1), ((53 % 10) + 1), 'Zadanie 53', 'Opis zadania 53', 'W toku', 'MEDIUM', '2025-06-22', '2025-07-06'),
  (54, ((54 % 20) + 1), ((54 % 10) + 1), 'Zadanie 54', 'Opis zadania 54', 'Zakończone', 'MEDIUM', '2025-06-23', '2025-07-07'),
  (55, ((55 % 20) + 1), ((55 % 10) + 1), 'Zadanie 55', 'Opis zadania 55', 'Nowe', 'MEDIUM', '2025-06-24', '2025-07-08'),
  (56, ((56 % 20) + 1), ((56 % 10) + 1), 'Zadanie 56', 'Opis zadania 56', 'W toku', 'MEDIUM', '2025-06-25', '2025-07-09'),
  (57, ((57 % 20) + 1), ((57 % 10) + 1), 'Zadanie 57', 'Opis zadania 57', 'Zakończone', 'MEDIUM', '2025-06-26', '2025-07-10'),
  (58, ((58 % 20) + 1), ((58 % 10) + 1), 'Zadanie 58', 'Opis zadania 58', 'Nowe', 'MEDIUM', '2025-06-27', '2025-07-11'),
  (59, ((59 % 20) + 1), ((59 % 10) + 1), 'Zadanie 59', 'Opis zadania 59', 'W toku', 'MEDIUM', '2025-06-28', '2025-07-12'),
  (60, ((60 % 20) + 1), ((60 % 10) + 1), 'Zadanie 60', 'Opis zadania 60', 'Zakończone', 'MEDIUM', '2025-06-29', '2025-07-13');

-- Przypisanie zadań do użytkowników - rozszerzamy istniejącą listę
INSERT INTO task_assignments (task_id, user_id) VALUES
  (1, 17),
  (2, 18),
  (3, 19),
  (4, 20),
  (5, 21),
  (6, 22),
  (7, 23),
  (8, 24),
  (9, 25),
  (10, 26),
  (11, 27),
  (12, 28),
  (13, 29),
  (14, 30),
  (15, 31),
  (16, 32),
  (17, 33),
  (18, 34),
  (19, 35),
  (20, 36),
  (21, 37),
  (22, 38),
  (23, 39),
  (24, 40),
  (25, 17),
  (26, 18),
  (27, 19),
  (28, 20),
  (29, 21),
  (30, 22),
  (31, 23),
  (32, 24),
  (33, 25),
  (34, 26),
  (35, 27),
  (36, 28),
  (37, 29),
  (38, 30),
  (39, 31),
  (40, 32),
  (41, 33),
  (42, 34),
  (43, 35),
  (44, 36),
  (45, 37),
  (46, 38),
  (47, 39),
  (48, 40),
  (49, 17),
  (50, 18),
  (51, 19),
  (52, 20),
  (53, 21),
  (54, 22),
  (55, 23),
  (56, 24),
  (57, 25),
  (58, 26),
  (59, 27),
  (60, 28);

-- Powiadomienia dla użytkowników
INSERT INTO notifications (user_id, content, is_read, created_at) VALUES
  (1, 'Przydzielono Ci nowe zadanie', 0, CURRENT_TIMESTAMP()),
  (2, 'Termin Twojego zadania mija za 2 dni', 0, CURRENT_TIMESTAMP()),
  (3, 'Kierownik skomentował Twoje zadanie', 0, CURRENT_TIMESTAMP()),
  (4, 'Raport miesięczny jest gotowy do przeglądu', 0, CURRENT_TIMESTAMP()),
  (5, 'Przydzielono Cię do nowego projektu', 0, CURRENT_TIMESTAMP()),
  (6, 'Zadanie zostało zakończone', 0, CURRENT_TIMESTAMP()),
  (7, 'Nowy użytkownik został dodany do Twojego zespołu', 0, CURRENT_TIMESTAMP()),
  (8, 'Przypomnienie o spotkaniu zespołu jutro o 10:00', 0, CURRENT_TIMESTAMP()),
  (9, 'Twoje zadanie zostało zmodyfikowane', 0, CURRENT_TIMESTAMP()),
  (10, 'Dodano nowy komentarz do Twojego zadania', 0, CURRENT_TIMESTAMP()),
  (11, 'Przydzielono Ci nowe zadanie', 0, CURRENT_TIMESTAMP()),
  (12, 'Termin projektu został zmieniony', 0, CURRENT_TIMESTAMP()),
  (13, 'Nowy dokument został dodany do projektu', 0, CURRENT_TIMESTAMP()),
  (14, 'Zadanie wymaga Twojej uwagi', 0, CURRENT_TIMESTAMP()),
  (15, 'Jeden z członków zespołu potrzebuje wsparcia', 0, CURRENT_TIMESTAMP()),
  (16, 'Zaktualizowano priorytety zadań w projekcie', 0, CURRENT_TIMESTAMP()),
  (17, 'Otrzymałeś nową wiadomość od kierownika', 0, CURRENT_TIMESTAMP()),
  (18, 'Twoje zgłoszenie zostało rozpatrzone', 0, CURRENT_TIMESTAMP()),
  (19, 'Zadanie zostało przekazane do innego zespołu', 0, CURRENT_TIMESTAMP()),
  (20, 'Twój raport został zaakceptowany', 0, CURRENT_TIMESTAMP());

-- Raporty (przykładowe dane)
INSERT INTO reports (report_name, report_type, report_scope, created_by, created_at, exported_file) VALUES
  ('Raport miesięczny - Maj 2025', 'miesięczny', 'Projekty 1-5', 4, '2025-05-31 15:30:00', 'raport_maj_2025.pdf'),
  ('Analiza wydajności zespołu Alfa', 'analiza', 'Zespół Alfa', 5, '2025-05-15 10:20:00', 'zespol_alfa_analiza.pdf'),
  ('Status projektów Q2 2025', 'kwartalny', 'Wszystkie projekty', 6, '2025-06-10 14:45:00', 'status_q2_2025.pdf'),
  ('Raport zadań opóźnionych', 'ad-hoc', 'Zadania ze statusem Opóźnione', 4, '2025-06-05 09:15:00', 'zadania_opoznione.pdf'),
  ('Obciążenie pracowników - Czerwiec 2025', 'miesięczny', 'Wszyscy pracownicy', 5, '2025-06-30 16:00:00', 'obciazenie_czerwiec_2025.pdf');

-- Trigger – aktualizacja pola last_password_change w tabeli settings
DELIMITER //

CREATE TRIGGER trg_update_password_change
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    -- Sprawdzamy, czy hasło zostało zmienione
    IF NEW.password <> OLD.password THEN
        -- Aktualizujemy timestamp w tabeli settings
        UPDATE settings
        SET last_password_change = CURRENT_TIMESTAMP()
        WHERE user_id = NEW.id;

        -- Jeśli nie istnieje rekord w settings dla tego użytkownika, wstawiamy nowy
        IF ROW_COUNT() = 0 THEN
            INSERT INTO settings (user_id, last_password_change)
            VALUES (NEW.id, CURRENT_TIMESTAMP());
        END IF;
    END IF;
END//

DELIMITER ;