INSERT INTO roles (id, role_name, permissions) VALUES
  (1, 'Team Lider', 'VIEW_TASKS,CREATE_TASKS,ASSIGN_TASKS,UPDATE_TASKS'),
  (2, 'Kierownik', 'VIEW_TASKS,UPDATE_TASKS,MANAGE_TEAMS,MANAGE_PROJECTS,VIEW_REPORTS'),
  (3, 'Administrator', 'VIEW_TASKS,CREATE_TASKS,UPDATE_TASKS,DELETE_TASKS,MANAGE_USERS,MANAGE_TEAMS,MANAGE_PROJECTS,VIEW_REPORTS'),
  (4, 'Pracownik', 'VIEW_TASKS,UPDATE_OWN_TASKS');

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

-- Administratorzy (3)
INSERT INTO users (id, name, last_name, email, password, password_hint, role_id, group_id) VALUES
  (1, 'Jan', 'Kowalski', 'jan.kowalski@firma.pl', 'haslo1', 'podpowiedz1', 3, 5),
  (2, 'Anna', 'Nowak', 'anna.nowak@firma.pl', 'haslo2', 'podpowiedz2', 3, 5),
  (3, 'Piotr', 'Wiśniewski', 'piotr.wisniewski@firma.pl', 'haslo3', 'podpowiedz3', 3, 5);

-- Kierownicy (3)
INSERT INTO users (id, name, last_name, email, password, password_hint, role_id, group_id) VALUES
  (4, 'Katarzyna', 'Zielińska', 'katarzyna.zielinska@firma.pl', 'haslo4', 'podpowiedz4', 2, 5),
  (5, 'Tomasz', 'Wójcik', 'tomasz.wojcik@firma.pl', 'haslo5', 'podpowiedz5', 2, 5),
  (6, 'Agnieszka', 'Krawczyk', 'agnieszka.krawczyk@firma.pl', 'haslo6', 'podpowiedz6', 2, 5);

-- Team Liderzy (10)
INSERT INTO users (id, name, last_name, email, password, password_hint, role_id, group_id) VALUES
  (7, 'Michał', 'Lewandowski', 'michal.lewandowski@firma.pl', 'haslo7', 'podpowiedz7', 1, 1),
  (8, 'Monika', 'Szymańska', 'monika.szymansk@firma.pl', 'haslo8', 'podpowiedz8', 1, 2),
  (9, 'Paweł', 'Dąbrowski', 'pawel.dabrowski@firma.pl', 'haslo9', 'podpowiedz9', 1, 1),
  (10, 'Ewa', 'Kozłowska', 'ewa.kozlowska@firma.pl', 'haslo10', 'podpowiedz10', 1, 3),
  (11, 'Krzysztof', 'Jankowski', 'krzysztof.jankowski@firma.pl', 'haslo11', 'podpowiedz11', 1, 2),
  (12, 'Barbara', 'Wojciechowska', 'barbara.wojciechowska@firma.pl', 'haslo12', 'podpowiedz12', 1, 3),
  (13, 'Adam', 'Kubiak', 'adam.kubiak@firma.pl', 'haslo13', 'podpowiedz13', 1, 1),
  (14, 'Elżbieta', 'Wróblewska', 'elzbieta.wroblewska@firma.pl', 'haslo14', 'podpowiedz14', 1, 2),
  (15, 'Robert', 'Nowicki', 'robert.nowicki@firma.pl', 'haslo15', 'podpowiedz15', 1, 3),
  (16, 'Iwona', 'Mazur', 'iwona.mazur@firma.pl', 'haslo16', 'podpowiedz16', 1, 1);

-- Pracownicy (24)
INSERT INTO users (id, name, last_name, email, password, password_hint, role_id, group_id) VALUES
  (17, 'Łukasz', 'Grabowski', 'lukasz.grabowski@firma.pl', 'haslo17', 'podpowiedz17', 4, 1),
  (18, 'Karolina', 'Nowakowska', 'karolina.nowakowska@firma.pl', 'haslo18', 'podpowiedz18', 4, 2),
  (19, 'Sebastian', 'Walczak', 'sebastian.walczak@firma.pl', 'haslo19', 'podpowiedz19', 4, 3),
  (20, 'Julia', 'Baran', 'julia.baran@firma.pl', 'haslo20', 'podpowiedz20', 4, 4),
  (21, 'Damian', 'Sikorski', 'damian.sikorski@firma.pl', 'haslo21', 'podpowiedz21', 4, 6),
  (22, 'Natalia', 'Pawłowska', 'natalia.pawlowska@firma.pl', 'haslo22', 'podpowiedz22', 4, 7),
  (23, 'Marek', 'Zawadzki', 'marek.zawadzki@firma.pl', 'haslo23', 'podpowiedz23', 4, 8),
  (24, 'Dorota', 'Piotrowska', 'dorota.piotrowska@firma.pl', 'haslo24', 'podpowiedz24', 4, 9),
  (25, 'Szymon', 'Król', 'szymon.krol@firma.pl', 'haslo25', 'podpowiedz25', 4, 10),
  (26, 'Weronika', 'Majewska', 'weronika.majewska@firma.pl', 'haslo26', 'podpowiedz26', 4, 1),
  (27, 'Oskar', 'Górski', 'oskar.gorski@firma.pl', 'haslo27', 'podpowiedz27', 4, 2),
  (28, 'Renata', 'Walczak', 'renata.walczak@firma.pl', 'haslo28', 'podpowiedz28', 4, 3),
  (29, 'Adrian', 'Stępień', 'adrian.stepien@firma.pl', 'haslo29', 'podpowiedz29', 4, 4),
  (30, 'Klaudia', 'Michalska', 'klaudia.michalska@firma.pl', 'haslo30', 'podpowiedz30', 4, 6),
  (31, 'Igor', 'Lis', 'igor.lis@firma.pl', 'haslo31', 'podpowiedz31', 4, 7),
  (32, 'Agata', 'Wójcik', 'agata.wojcik@firma.pl', 'haslo32', 'podpowiedz32', 4, 8),
  (33, 'Filip', 'Brzeziński', 'filip.brzezinski@firma.pl', 'haslo33', 'podpowiedz33', 4, 9),
  (34, 'Martyna', 'Nowak', 'martyna.nowak@firma.pl', 'haslo34', 'podpowiedz34', 4, 10),
  (35, 'Konstanty', 'Ostrowski', 'konstanty.ostrowski@firma.pl', 'haslo35', 'podpowiedz35', 4, 1),
  (36, 'Emilia', 'Adamczyk', 'emilia.adamczyk@firma.pl', 'haslo36', 'podpowiedz36', 4, 2),
  (37, 'Dawid', 'Jasiński', 'dawid.jasinski@firma.pl', 'haslo37', 'podpowiedz37', 4, 3),
  (38, 'Irek', 'Barczak', 'irek.barczak@firma.pl', 'haslo38', 'podpowiedz38', 4, 4),
  (39, 'Julia', 'Kowalczyk', 'julia.kowalczyk@firma.pl', 'haslo39', 'podpowiedz39', 4, 6),
  (40, 'Sylwia', 'Głowacka', 'sylwia.glowacka@firma.pl', 'haslo40', 'podpowiedz40', 4, 7);

INSERT INTO projects (id, project_name, description, start_date, end_date, manager_id) VALUES
  (1, 'Projekt 1', 'Opis projektu 1', '2025-05-01', '2025-07-01', 4),
  (2, 'Projekt 2', 'Opis projektu 2', '2025-05-05', '2025-07-05', 5),
  (3, 'Projekt 3', 'Opis projektu 3', '2025-05-10', '2025-07-10', 6),
  (4, 'Projekt 4', 'Opis projektu 4', '2025-05-15', '2025-07-15', 4),
  (5, 'Projekt 5', 'Opis projektu 5', '2025-05-20', '2025-07-20', 5),
  (6, 'Projekt 6', 'Opis projektu 6', '2025-05-25', '2025-07-25', 6),
  (7, 'Projekt 7', 'Opis projektu 7', '2025-06-01', '2025-08-01', 4),
  (8, 'Projekt 8', 'Opis projektu 8', '2025-06-05', '2025-08-05', 5),
  (9, 'Projekt 9', 'Opis projektu 9', '2025-06-10', '2025-08-10', 6),
  (10, 'Projekt 10', 'Opis projektu 10', '2025-06-15', '2025-08-15', 4),
  (11, 'Projekt 11', 'Opis projektu 11', '2025-06-20', '2025-08-20', 5),
  (12, 'Projekt 12', 'Opis projektu 12', '2025-06-25', '2025-08-25', 6),
  (13, 'Projekt 13', 'Opis projektu 13', '2025-07-01', '2025-09-01', 4),
  (14, 'Projekt 14', 'Opis projektu 14', '2025-07-05', '2025-09-05', 5),
  (15, 'Projekt 15', 'Opis projektu 15', '2025-07-10', '2025-09-10', 6),
  (16, 'Projekt 16', 'Opis projektu 16', '2025-07-15', '2025-09-15', 4),
  (17, 'Projekt 17', 'Opis projektu 17', '2025-07-20', '2025-09-20', 5),
  (18, 'Projekt 18', 'Opis projektu 18', '2025-07-25', '2025-09-25', 6),
  (19, 'Projekt 19', 'Opis projektu 19', '2025-08-01', '2025-10-01', 4),
  (20, 'Projekt 20', 'Opis projektu 20', '2025-08-05', '2025-10-05', 5);


INSERT INTO teams (id, team_name, project_id) VALUES
  (1, 'Zespół Alfa', 1),
  (2, 'Zespół Beta', 2),
  (3, 'Zespół Gamma', 3),
  (4, 'Zespół Delta', 4),
  (5, 'Zespół Epsilon', 5),
  (6, 'Zespół Zeta', 6),
  (7, 'Zespół Eta', 7),
  (8, 'Zespół Theta', 8),
  (9, 'Zespół Iota', 9),
  (10, 'Zespół Downa', 10);

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
  (1, ((1 % 20) + 1), ((1 % 10) + 1), 'Zadanie 1', 'Opis zadania 1', 'TO_DO', 'MEDIUM', '2025-05-01', '2025-05-15'),
  (2, ((2 % 20) + 1), ((2 % 10) + 1), 'Zadanie 2', 'Opis zadania 2', 'TO_DO', 'MEDIUM', '2025-05-02', '2025-05-16'),
  (3, ((3 % 20) + 1), ((3 % 10) + 1), 'Zadanie 3', 'Opis zadania 3', 'TO_DO', 'MEDIUM', '2025-05-03', '2025-05-17'),
  (4, ((4 % 20) + 1), ((4 % 10) + 1), 'Zadanie 4', 'Opis zadania 4', 'TO_DO', 'MEDIUM', '2025-05-04', '2025-05-18'),
  (5, ((5 % 20) + 1), ((5 % 10) + 1), 'Zadanie 5', 'Opis zadania 5', 'TO_DO', 'MEDIUM', '2025-05-05', '2025-05-19'),
  (6, ((6 % 20) + 1), ((6 % 10) + 1), 'Zadanie 6', 'Opis zadania 6', 'TO_DO', 'MEDIUM', '2025-05-06', '2025-05-20'),
  (7, ((7 % 20) + 1), ((7 % 10) + 1), 'Zadanie 7', 'Opis zadania 7', 'TO_DO', 'MEDIUM', '2025-05-07', '2025-05-21'),
  (8, ((8 % 20) + 1), ((8 % 10) + 1), 'Zadanie 8', 'Opis zadania 8', 'TO_DO', 'MEDIUM', '2025-05-08', '2025-05-22'),
  (9, ((9 % 20) + 1), ((9 % 10) + 1), 'Zadanie 9', 'Opis zadania 9', 'TO_DO', 'MEDIUM', '2025-05-09', '2025-05-23'),
  (10, ((10 % 20) + 1), ((10 % 10) + 1), 'Zadanie 10', 'Opis zadania 10', 'TO_DO', 'MEDIUM', '2025-05-10', '2025-05-24'),
  (11, ((11 % 20) + 1), ((11 % 10) + 1), 'Zadanie 11', 'Opis zadania 11', 'TO_DO', 'MEDIUM', '2025-05-11', '2025-05-25'),
  (12, ((12 % 20) + 1), ((12 % 10) + 1), 'Zadanie 12', 'Opis zadania 12', 'TO_DO', 'MEDIUM', '2025-05-12', '2025-05-26'),
  (13, ((13 % 20) + 1), ((13 % 10) + 1), 'Zadanie 13', 'Opis zadania 13', 'TO_DO', 'MEDIUM', '2025-05-13', '2025-05-27'),
  (14, ((14 % 20) + 1), ((14 % 10) + 1), 'Zadanie 14', 'Opis zadania 14', 'TO_DO', 'MEDIUM', '2025-05-14', '2025-05-28'),
  (15, ((15 % 20) + 1), ((15 % 10) + 1), 'Zadanie 15', 'Opis zadania 15', 'TO_DO', 'MEDIUM', '2025-05-15', '2025-05-29'),
  (16, ((16 % 20) + 1), ((16 % 10) + 1), 'Zadanie 16', 'Opis zadania 16', 'TO_DO', 'MEDIUM', '2025-05-16', '2025-05-30'),
  (17, ((17 % 20) + 1), ((17 % 10) + 1), 'Zadanie 17', 'Opis zadania 17', 'TO_DO', 'MEDIUM', '2025-05-17', '2025-05-31'),
  (18, ((18 % 20) + 1), ((18 % 10) + 1), 'Zadanie 18', 'Opis zadania 18', 'TO_DO', 'MEDIUM', '2025-05-18', '2025-06-01'),
  (19, ((19 % 20) + 1), ((19 % 10) + 1), 'Zadanie 19', 'Opis zadania 19', 'TO_DO', 'MEDIUM', '2025-05-19', '2025-06-02'),
  (20, ((20 % 20) + 1), ((20 % 10) + 1), 'Zadanie 20', 'Opis zadania 20', 'TO_DO', 'MEDIUM', '2025-05-20', '2025-06-03'),
  (21, ((21 % 20) + 1), ((21 % 10) + 1), 'Zadanie 21', 'Opis zadania 21', 'TO_DO', 'MEDIUM', '2025-05-21', '2025-06-04'),
  (22, ((22 % 20) + 1), ((22 % 10) + 1), 'Zadanie 22', 'Opis zadania 22', 'TO_DO', 'MEDIUM', '2025-05-22', '2025-06-05'),
  (23, ((23 % 20) + 1), ((23 % 10) + 1), 'Zadanie 23', 'Opis zadania 23', 'TO_DO', 'MEDIUM', '2025-05-23', '2025-06-06'),
  (24, ((24 % 20) + 1), ((24 % 10) + 1), 'Zadanie 24', 'Opis zadania 24', 'TO_DO', 'MEDIUM', '2025-05-24', '2025-06-07'),
  (25, ((25 % 20) + 1), ((25 % 10) + 1), 'Zadanie 25', 'Opis zadania 25', 'TO_DO', 'MEDIUM', '2025-05-25', '2025-06-08'),
  (26, ((26 % 20) + 1), ((26 % 10) + 1), 'Zadanie 26', 'Opis zadania 26', 'TO_DO', 'MEDIUM', '2025-05-26', '2025-06-09'),
  (27, ((27 % 20) + 1), ((27 % 10) + 1), 'Zadanie 27', 'Opis zadania 27', 'TO_DO', 'MEDIUM', '2025-05-27', '2025-06-10'),
  (28, ((28 % 20) + 1), ((28 % 10) + 1), 'Zadanie 28', 'Opis zadania 28', 'TO_DO', 'MEDIUM', '2025-05-28', '2025-06-11'),
  (29, ((29 % 20) + 1), ((29 % 10) + 1), 'Zadanie 29', 'Opis zadania 29', 'TO_DO', 'MEDIUM', '2025-05-29', '2025-06-12'),
  (30, ((30 % 20) + 1), ((30 % 10) + 1), 'Zadanie 30', 'Opis zadania 30', 'TO_DO', 'MEDIUM', '2025-05-30', '2025-06-13'),
  (31, ((31 % 20) + 1), ((31 % 10) + 1), 'Zadanie 31', 'Opis zadania 31', 'TO_DO', 'MEDIUM', '2025-05-31', '2025-06-14'),
  (32, ((32 % 20) + 1), ((32 % 10) + 1), 'Zadanie 32', 'Opis zadania 32', 'TO_DO', 'MEDIUM', '2025-06-01', '2025-06-15'),
  (33, ((33 % 20) + 1), ((33 % 10) + 1), 'Zadanie 33', 'Opis zadania 33', 'TO_DO', 'MEDIUM', '2025-06-02', '2025-06-16'),
  (34, ((34 % 20) + 1), ((34 % 10) + 1), 'Zadanie 34', 'Opis zadania 34', 'TO_DO', 'MEDIUM', '2025-06-03', '2025-06-17'),
  (35, ((35 % 20) + 1), ((35 % 10) + 1), 'Zadanie 35', 'Opis zadania 35', 'TO_DO', 'MEDIUM', '2025-06-04', '2025-06-18'),
  (36, ((36 % 20) + 1), ((36 % 10) + 1), 'Zadanie 36', 'Opis zadania 36', 'TO_DO', 'MEDIUM', '2025-06-05', '2025-06-19'),
  (37, ((37 % 20) + 1), ((37 % 10) + 1), 'Zadanie 37', 'Opis zadania 37', 'TO_DO', 'MEDIUM', '2025-06-06', '2025-06-20'),
  (38, ((38 % 20) + 1), ((38 % 10) + 1), 'Zadanie 38', 'Opis zadania 38', 'TO_DO', 'MEDIUM', '2025-06-07', '2025-06-21'),
  (39, ((39 % 20) + 1), ((39 % 10) + 1), 'Zadanie 39', 'Opis zadania 39', 'TO_DO', 'MEDIUM', '2025-06-08', '2025-06-22'),
  (40, ((40 % 20) + 1), ((40 % 10) + 1), 'Zadanie 40', 'Opis zadania 40', 'TO_DO', 'MEDIUM', '2025-06-09', '2025-06-23'),
  (41, ((41 % 20) + 1), ((41 % 10) + 1), 'Zadanie 41', 'Opis zadania 41', 'TO_DO', 'MEDIUM', '2025-06-10', '2025-06-24'),
  (42, ((42 % 20) + 1), ((42 % 10) + 1), 'Zadanie 42', 'Opis zadania 42', 'TO_DO', 'MEDIUM', '2025-06-11', '2025-06-25'),
  (43, ((43 % 20) + 1), ((43 % 10) + 1), 'Zadanie 43', 'Opis zadania 43', 'TO_DO', 'MEDIUM', '2025-06-12', '2025-06-26'),
  (44, ((44 % 20) + 1), ((44 % 10) + 1), 'Zadanie 44', 'Opis zadania 44', 'TO_DO', 'MEDIUM', '2025-06-13', '2025-06-27'),
  (45, ((45 % 20) + 1), ((45 % 10) + 1), 'Zadanie 45', 'Opis zadania 45', 'TO_DO', 'MEDIUM', '2025-06-14', '2025-06-28'),
  (46, ((46 % 20) + 1), ((46 % 10) + 1), 'Zadanie 46', 'Opis zadania 46', 'TO_DO', 'MEDIUM', '2025-06-15', '2025-06-29'),
  (47, ((47 % 20) + 1), ((47 % 10) + 1), 'Zadanie 47', 'Opis zadania 47', 'TO_DO', 'MEDIUM', '2025-06-16', '2025-06-30'),
  (48, ((48 % 20) + 1), ((48 % 10) + 1), 'Zadanie 48', 'Opis zadania 48', 'TO_DO', 'MEDIUM', '2025-06-17', '2025-07-01'),
  (49, ((49 % 20) + 1), ((49 % 10) + 1), 'Zadanie 49', 'Opis zadania 49', 'TO_DO', 'MEDIUM', '2025-06-18', '2025-07-02'),
  (50, ((50 % 20) + 1), ((50 % 10) + 1), 'Zadanie 50', 'Opis zadania 50', 'TO_DO', 'MEDIUM', '2025-06-19', '2025-07-03'),
  (51, ((51 % 20) + 1), ((51 % 10) + 1), 'Zadanie 51', 'Opis zadania 51', 'TO_DO', 'MEDIUM', '2025-06-20', '2025-07-04'),
  (52, ((52 % 20) + 1), ((52 % 10) + 1), 'Zadanie 52', 'Opis zadania 52', 'TO_DO', 'MEDIUM', '2025-06-21', '2025-07-05'),
  (53, ((53 % 20) + 1), ((53 % 10) + 1), 'Zadanie 53', 'Opis zadania 53', 'TO_DO', 'MEDIUM', '2025-06-22', '2025-07-06'),
  (54, ((54 % 20) + 1), ((54 % 10) + 1), 'Zadanie 54', 'Opis zadania 54', 'TO_DO', 'MEDIUM', '2025-06-23', '2025-07-07'),
  (55, ((55 % 20) + 1), ((55 % 10) + 1), 'Zadanie 55', 'Opis zadania 55', 'TO_DO', 'MEDIUM', '2025-06-24', '2025-07-08'),
  (56, ((56 % 20) + 1), ((56 % 10) + 1), 'Zadanie 56', 'Opis zadania 56', 'TO_DO', 'MEDIUM', '2025-06-25', '2025-07-09'),
  (57, ((57 % 20) + 1), ((57 % 10) + 1), 'Zadanie 57', 'Opis zadania 57', 'TO_DO', 'MEDIUM', '2025-06-26', '2025-07-10'),
  (58, ((58 % 20) + 1), ((58 % 10) + 1), 'Zadanie 58', 'Opis zadania 58', 'TO_DO', 'MEDIUM', '2025-06-27', '2025-07-11'),
  (59, ((59 % 20) + 1), ((59 % 10) + 1), 'Zadanie 59', 'Opis zadania 59', 'TO_DO', 'MEDIUM', '2025-06-28', '2025-07-12'),
  (60, ((60 % 20) + 1), ((60 % 10) + 1), 'Zadanie 60', 'Opis zadania 60', 'TO_DO', 'MEDIUM', '2025-06-29', '2025-07-13');

-- Przykładowe typy aktywności: 'Komentarz', 'Aktualizacja', 'Załącznik dodany'
-- Przypisujemy: task_id = ((id % 60)+1), user_id = ((id % 40)+1)
INSERT INTO task_activities (id, task_id, user_id, activity_type, description, created_at) VALUES
  (1, ((1 % 60) + 1), ((1 % 40) + 1), 'Komentarz', 'Aktywność 1 dla zadania 1', NOW()),
  (2, ((2 % 60) + 1), ((2 % 40) + 1), 'Aktualizacja', 'Aktywność 2 dla zadania 2', NOW()),
  (3, ((3 % 60) + 1), ((3 % 40) + 1), 'Załącznik dodany', 'Aktywność 3 dla zadania 3', NOW()),
  (4, ((4 % 60) + 1), ((4 % 40) + 1), 'Komentarz', 'Aktywność 4 dla zadania 4', NOW()),
  (5, ((5 % 60) + 1), ((5 % 40) + 1), 'Aktualizacja', 'Aktywność 5 dla zadania 5', NOW()),
  (6, ((6 % 60) + 1), ((6 % 40) + 1), 'Załącznik dodany', 'Aktywność 6 dla zadania 6', NOW()),
  (7, ((7 % 60) + 1), ((7 % 40) + 1), 'Komentarz', 'Aktywność 7 dla zadania 7', NOW()),
  (8, ((8 % 60) + 1), ((8 % 40) + 1), 'Aktualizacja', 'Aktywność 8 dla zadania 8', NOW()),
  (9, ((9 % 60) + 1), ((9 % 40) + 1), 'Załącznik dodany', 'Aktywność 9 dla zadania 9', NOW()),
  (10, ((10 % 60) + 1), ((10 % 40) + 1), 'Komentarz', 'Aktywność 10 dla zadania 10', NOW()),
  (11, ((11 % 60) + 1), ((11 % 40) + 1), 'Aktualizacja', 'Aktywność 11 dla zadania 11', NOW()),
  (12, ((12 % 60) + 1), ((12 % 40) + 1), 'Załącznik dodany', 'Aktywność 12 dla zadania 12', NOW()),
  (13, ((13 % 60) + 1), ((13 % 40) + 1), 'Komentarz', 'Aktywność 13 dla zadania 13', NOW()),
  (14, ((14 % 60) + 1), ((14 % 40) + 1), 'Aktualizacja', 'Aktywność 14 dla zadania 14', NOW()),
  (15, ((15 % 60) + 1), ((15 % 40) + 1), 'Załącznik dodany', 'Aktywność 15 dla zadania 15', NOW()),
  (16, ((16 % 60) + 1), ((16 % 40) + 1), 'Komentarz', 'Aktywność 16 dla zadania 16', NOW()),
  (17, ((17 % 60) + 1), ((17 % 40) + 1), 'Aktualizacja', 'Aktywność 17 dla zadania 17', NOW()),
  (18, ((18 % 60) + 1), ((18 % 40) + 1), 'Załącznik dodany', 'Aktywność 18 dla zadania 18', NOW()),
  (19, ((19 % 60) + 1), ((19 % 40) + 1), 'Komentarz', 'Aktywność 19 dla zadania 19', NOW()),
  (20, ((20 % 60) + 1), ((20 % 40) + 1), 'Aktualizacja', 'Aktywność 20 dla zadania 20', NOW()),
  (21, ((21 % 60) + 1), ((21 % 40) + 1), 'Załącznik dodany', 'Aktywność 21 dla zadania 21', NOW()),
  (22, ((22 % 60) + 1), ((22 % 40) + 1), 'Komentarz', 'Aktywność 22 dla zadania 22', NOW()),
  (23, ((23 % 60) + 1), ((23 % 40) + 1), 'Aktualizacja', 'Aktywność 23 dla zadania 23', NOW()),
  (24, ((24 % 60) + 1), ((24 % 40) + 1), 'Załącznik dodany', 'Aktywność 24 dla zadania 24', NOW()),
  (25, ((25 % 60) + 1), ((25 % 40) + 1), 'Komentarz', 'Aktywność 25 dla zadania 25', NOW()),
  (26, ((26 % 60) + 1), ((26 % 40) + 1), 'Aktualizacja', 'Aktywność 26 dla zadania 26', NOW()),
  (27, ((27 % 60) + 1), ((27 % 40) + 1), 'Załącznik dodany', 'Aktywność 27 dla zadania 27', NOW()),
  (28, ((28 % 60) + 1), ((28 % 40) + 1), 'Komentarz', 'Aktywność 28 dla zadania 28', NOW()),
  (29, ((29 % 60) + 1), ((29 % 40) + 1), 'Aktualizacja', 'Aktywność 29 dla zadania 29', NOW()),
  (30, ((30 % 60) + 1), ((30 % 40) + 1), 'Załącznik dodany', 'Aktywność 30 dla zadania 30', NOW()),
  (31, ((31 % 60) + 1), ((31 % 40) + 1), 'Komentarz', 'Aktywność 31 dla zadania 31', NOW()),
  (32, ((32 % 60) + 1), ((32 % 40) + 1), 'Aktualizacja', 'Aktywność 32 dla zadania 32', NOW()),
  (33, ((33 % 60) + 1), ((33 % 40) + 1), 'Załącznik dodany', 'Aktywność 33 dla zadania 33', NOW()),
  (34, ((34 % 60) + 1), ((34 % 40) + 1), 'Komentarz', 'Aktywność 34 dla zadania 34', NOW()),
  (35, ((35 % 60) + 1), ((35 % 40) + 1), 'Aktualizacja', 'Aktywność 35 dla zadania 35', NOW()),
  (36, ((36 % 60) + 1), ((36 % 40) + 1), 'Załącznik dodany', 'Aktywność 36 dla zadania 36', NOW()),
  (37, ((37 % 60) + 1), ((37 % 40) + 1), 'Komentarz', 'Aktywność 37 dla zadania 37', NOW()),
  (38, ((38 % 60) + 1), ((38 % 40) + 1), 'Aktualizacja', 'Aktywność 38 dla zadania 38', NOW()),
  (39, ((39 % 60) + 1), ((39 % 40) + 1), 'Załącznik dodany', 'Aktywność 39 dla zadania 39', NOW()),
  (40, ((40 % 60) + 1), ((40 % 40) + 1), 'Komentarz', 'Aktywność 40 dla zadania 40', NOW()),
  (41, ((41 % 60) + 1), ((41 % 40) + 1), 'Aktualizacja', 'Aktywność 41 dla zadania 41', NOW()),
  (42, ((42 % 60) + 1), ((42 % 40) + 1), 'Załącznik dodany', 'Aktywność 42 dla zadania 42', NOW()),
  (43, ((43 % 60) + 1), ((43 % 40) + 1), 'Komentarz', 'Aktywność 43 dla zadania 43', NOW()),
  (44, ((44 % 60) + 1), ((44 % 40) + 1), 'Aktualizacja', 'Aktywność 44 dla zadania 44', NOW()),
  (45, ((45 % 60) + 1), ((45 % 40) + 1), 'Załącznik dodany', 'Aktywność 45 dla zadania 45', NOW()),
  (46, ((46 % 60) + 1), ((46 % 40) + 1), 'Komentarz', 'Aktywność 46 dla zadania 46', NOW()),
  (47, ((47 % 60) + 1), ((47 % 40) + 1), 'Aktualizacja', 'Aktywność 47 dla zadania 47', NOW()),
  (48, ((48 % 60) + 1), ((48 % 40) + 1), 'Załącznik dodany', 'Aktywność 48 dla zadania 48', NOW()),
  (49, ((49 % 60) + 1), ((49 % 40) + 1), 'Komentarz', 'Aktywność 49 dla zadania 49', NOW()),
  (50, ((50 % 60) + 1), ((50 % 40) + 1), 'Aktualizacja', 'Aktywność 50 dla zadania 50', NOW()),
  (51, ((51 % 60) + 1), ((51 % 40) + 1), 'Załącznik dodany', 'Aktywność 51 dla zadania 51', NOW()),
  (52, ((52 % 60) + 1), ((52 % 40) + 1), 'Komentarz', 'Aktywność 52 dla zadania 52', NOW()),
  (53, ((53 % 60) + 1), ((53 % 40) + 1), 'Aktualizacja', 'Aktywność 53 dla zadania 53', NOW()),
  (54, ((54 % 60) + 1), ((54 % 40) + 1), 'Załącznik dodany', 'Aktywność 54 dla zadania 54', NOW()),
  (55, ((55 % 60) + 1), ((55 % 40) + 1), 'Komentarz', 'Aktywność 55 dla zadania 55', NOW()),
  (56, ((56 % 60) + 1), ((56 % 40) + 1), 'Aktualizacja', 'Aktywność 56 dla zadania 56', NOW()),
  (57, ((57 % 60) + 1), ((57 % 40) + 1), 'Załącznik dodany', 'Aktywność 57 dla zadania 57', NOW()),
  (58, ((58 % 60) + 1), ((58 % 40) + 1), 'Komentarz', 'Aktywność 58 dla zadania 58', NOW()),
  (59, ((59 % 60) + 1), ((59 % 40) + 1), 'Aktualizacja', 'Aktywność 59 dla zadania 59', NOW()),
  (60, ((60 % 60) + 1), ((60 % 40) + 1), 'Załącznik dodany', 'Aktywność 60 dla zadania 60', NOW()),
  (61, ((61 % 60) + 1), ((61 % 40) + 1), 'Komentarz', 'Aktywność 61 dla zadania 61', NOW()),
  (62, ((62 % 60) + 1), ((62 % 40) + 1), 'Aktualizacja', 'Aktywność 62 dla zadania 62', NOW()),
  (63, ((63 % 60) + 1), ((63 % 40) + 1), 'Załącznik dodany', 'Aktywność 63 dla zadania 63', NOW()),
  (64, ((64 % 60) + 1), ((64 % 40) + 1), 'Komentarz', 'Aktywność 64 dla zadania 64', NOW()),
  (65, ((65 % 60) + 1), ((65 % 40) + 1), 'Aktualizacja', 'Aktywność 65 dla zadania 65', NOW()),
  (66, ((66 % 60) + 1), ((66 % 40) + 1), 'Załącznik dodany', 'Aktywność 66 dla zadania 66', NOW()),
  (67, ((67 % 60) + 1), ((67 % 40) + 1), 'Komentarz', 'Aktywność 67 dla zadania 67', NOW()),
  (68, ((68 % 60) + 1), ((68 % 40) + 1), 'Aktualizacja', 'Aktywność 68 dla zadania 68', NOW()),
  (69, ((69 % 60) + 1), ((69 % 40) + 1), 'Załącznik dodany', 'Aktywność 69 dla zadania 69', NOW()),
  (70, ((70 % 60) + 1), ((70 % 40) + 1), 'Komentarz', 'Aktywność 70 dla zadania 70', NOW()),
  (71, ((71 % 60) + 1), ((71 % 40) + 1), 'Aktualizacja', 'Aktywność 71 dla zadania 71', NOW()),
  (72, ((72 % 60) + 1), ((72 % 40) + 1), 'Załącznik dodany', 'Aktywność 72 dla zadania 72', NOW()),
  (73, ((73 % 60) + 1), ((73 % 40) + 1), 'Komentarz', 'Aktywność 73 dla zadania 73', NOW()),
  (74, ((74 % 60) + 1), ((74 % 40) + 1), 'Aktualizacja', 'Aktywność 74 dla zadania 74', NOW()),
  (75, ((75 % 60) + 1), ((75 % 40) + 1), 'Załącznik dodany', 'Aktywność 75 dla zadania 75', NOW()),
  (76, ((76 % 60) + 1), ((76 % 40) + 1), 'Komentarz', 'Aktywność 76 dla zadania 76', NOW()),
  (77, ((77 % 60) + 1), ((77 % 40) + 1), 'Aktualizacja', 'Aktywność 77 dla zadania 77', NOW()),
  (78, ((78 % 60) + 1), ((78 % 40) + 1), 'Załącznik dodany', 'Aktywność 78 dla zadania 78', NOW()),
  (79, ((79 % 60) + 1), ((79 % 40) + 1), 'Komentarz', 'Aktywność 79 dla zadania 79', NOW()),
  (80, ((80 % 60) + 1), ((80 % 40) + 1), 'Aktualizacja', 'Aktywność 80 dla zadania 80', NOW()),
  (81, ((81 % 60) + 1), ((81 % 40) + 1), 'Załącznik dodany', 'Aktywność 81 dla zadania 81', NOW()),
  (82, ((82 % 60) + 1), ((82 % 40) + 1), 'Komentarz', 'Aktywność 82 dla zadania 82', NOW()),
  (83, ((83 % 60) + 1), ((83 % 40) + 1), 'Aktualizacja', 'Aktywność 83 dla zadania 83', NOW()),
  (84, ((84 % 60) + 1), ((84 % 40) + 1), 'Załącznik dodany', 'Aktywność 84 dla zadania 84', NOW()),
  (85, ((85 % 60) + 1), ((85 % 40) + 1), 'Komentarz', 'Aktywność 85 dla zadania 85', NOW()),
  (86, ((86 % 60) + 1), ((86 % 40) + 1), 'Aktualizacja', 'Aktywność 86 dla zadania 86', NOW()),
  (87, ((87 % 60) + 1), ((87 % 40) + 1), 'Załącznik dodany', 'Aktywność 87 dla zadania 87', NOW()),
  (88, ((88 % 60) + 1), ((88 % 40) + 1), 'Komentarz', 'Aktywność 88 dla zadania 88', NOW()),
  (89, ((89 % 60) + 1), ((89 % 40) + 1), 'Aktualizacja', 'Aktywność 89 dla zadania 89', NOW()),
  (90, ((90 % 60) + 1), ((90 % 40) + 1), 'Załącznik dodany', 'Aktywność 90 dla zadania 90', NOW()),
  (91, ((91 % 60) + 1), ((91 % 40) + 1), 'Komentarz', 'Aktywność 91 dla zadania 91', NOW()),
  (92, ((92 % 60) + 1), ((92 % 40) + 1), 'Aktualizacja', 'Aktywność 92 dla zadania 92', NOW()),
  (93, ((93 % 60) + 1), ((93 % 40) + 1), 'Załącznik dodany', 'Aktywność 93 dla zadania 93', NOW()),
  (94, ((94 % 60) + 1), ((94 % 40) + 1), 'Komentarz', 'Aktywność 94 dla zadania 94', NOW()),
  (95, ((95 % 60) + 1), ((95 % 40) + 1), 'Aktualizacja', 'Aktywność 95 dla zadania 95', NOW()),
  (96, ((96 % 60) + 1), ((96 % 40) + 1), 'Załącznik dodany', 'Aktywność 96 dla zadania 96', NOW()),
  (97, ((97 % 60) + 1), ((97 % 40) + 1), 'Komentarz', 'Aktywność 97 dla zadania 97', NOW()),
  (98, ((98 % 60) + 1), ((98 % 40) + 1), 'Aktualizacja', 'Aktywność 98 dla zadania 98', NOW()),
  (99, ((99 % 60) + 1), ((99 % 40) + 1), 'Załącznik dodany', 'Aktywność 99 dla zadania 99', NOW()),
  (100, ((100 % 60) + 1), ((100 % 40) + 1), 'Komentarz', 'Aktywność 100 dla zadania 100', NOW());
