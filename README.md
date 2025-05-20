# INF_ROZOWI (Programowanie Zespołowe)

## Opis projektu

**INF_ROZOWI** to system zarządzania zadaniami dedykowany firmom informatycznym, tworzony w ramach przedmiotu **Programowanie Zespołowe**. Głównym celem jest usprawnienie planowania, monitorowania i raportowania zadań oraz projektów w codziennej pracy zespołu developerskiego.  
System wspiera **współpracę** poprzez klarowny podział ról – **Administratora**, **Kierownika** i **Użytkownika** (pracownika), zapewniając odpowiedni poziom dostępu i różne narzędzia do kontroli postępu pracy.

Rozwiązanie umożliwia także efektywne śledzenie historii zmian w kodzie, zarządzanie backupami i szczegółową kontrolę nad procesem wytwarzania oprogramowania.

---

## Skład zespołu

Projekt realizowany jest przez czterech członków zespołu:

- **Dawid** – odpowiedzialny za część **Frontend** (JavaFX, UI, animacje)
- **Piotr** – odpowiedzialny za **Backend** (logika biznesowa, integracja z bazą danych)
- **Łukasz** – odpowiedzialny za część **Bazy Danych** (projekt i implementacja DB)
- **Piotr** – wsparcie przy **Bazie Danych** (tworzenie zapytań, migracje, optymalizacja)

> Oprócz głównych ról każdy członek pomaga w testach i dokumentacji.

---

## Kluczowe funkcjonalności

1. **Moduł administracji użytkownikami**  
   Tworzenie, edycja, usuwanie kont i przypisywanie ról, aby zarządzać uprawnieniami w systemie.

2. **Moduł zarządzania zadaniami**  
   Umożliwia tworzenie, edytowanie, usuwanie oraz przypisywanie zadań pracownikom, a także śledzenie postępów.

3. **Moduł raportowania**  
   Generowanie raportów (np. w PDF) o postępach zadań i projektów, z możliwością filtrowania według kryteriów (status, priorytet, osoba odpowiedzialna, itp.).

4. **Moduł konfiguracji**  
   Dostosowanie ustawień systemowych, w tym definiowanie nowych ról, priorytetów i statusów zadań.

5. **Integracja z bazą danych**  
   Logika do zapisu i odczytu informacji o użytkownikach, projektach i zadaniach.

---

## Architektura systemu

Projekt opiera się na **architekturze warstwowej**, dzielącej się na:

- **Frontend (JavaFX)**  
  Warstwa prezentacji (interfejs użytkownika), zapewniająca intuicyjną obsługę systemu i przyjazne GUI.
- **Backend (logika biznesowa w Javie)**  
  Odpowiedzialna za przetwarzanie danych (tworzenie zadań, raportów, zarządzanie rolami) oraz komunikację z bazą.
- **Baza Danych**  
  Warstwa magazynowania informacji o użytkownikach, zadaniach, projektach i wygenerowanych raportach.

---

## Struktura katalogów

Przykładowe drzewo projektu:

```

CODE/App/
├─ .idea/
├─ .mvn/
├─ src/
│ └─ main/
│ ├─ java/
│ │ └─ pl/
│ │ └─ rozowi/
│ │ └─ app/
│ │ ├─ controllers/
│ │ │ ├─ LoginController.java
│ │ │ ├─ ManagerDashboardController.java
│ │ │ ├─ AdminDashboardController.java
│ │ │ ├─ UserDashboardController.java
│ │ │ └─ ...
│ │ ├─ MainApplication.java
│ │ └─ ...
│ └─ resources/
│ ├─ fxml/
│ │ ├─ login.fxml
│ │ ├─ register.fxml
│ │ ├─ userDashboard.fxml
│ │ ├─ managerDashboard.fxml
│ │ ├─ adminDashboard.fxml
│ │ ├─ admin/
│ │ │ ├─ userManagement.fxml
│ │ │ ├─ tasksManagement.fxml
│ │ │ ├─ config.fxml
│ │ │ └─ adminReports.fxml
│ │ ├─ manager/
│ │ │ └─ managerReports.fxml
│ │ └─ ...
│ ├─ images/
│ │ └─ logo.png
│ └─ ...
├─ target/
├─ .gitignore
├─ mvnw
├─ mvnw.cmd
├─ pom.xml
└─ README.md

```

- **src/main/java/**
  Kody źródłowe aplikacji w Javie (kontrolery, klasy modelu, logika biznesowa).
- **src/main/resources/**
  Zasoby: pliki FXML (interfejsy GUI), grafiki, pliki konfiguracyjne itp.
- **target/**
  Automatycznie generowany folder przez Maven (pliki wynikowe, .class, .jar).
- **pom.xml**
  Główny plik konfiguracyjny Mavena.

---

## Instalacja i uruchomienie

Aby uruchomić aplikację lokalnie:

1. **Sklonuj repozytorium**:

   ```bash
   git clone https://github.com/PZ-2025/INF_ROZOWI
   cd INF_ROZOWI
   ```

2. **Zbuduj projekt** (np. za pomocą Mavena):

   ```bash
   mvn clean install
   ```

3. **Uruchom aplikację**:
   - Bezpośrednio w IDE (np. IntelliJ) – wybierz klasę `MainApplication` i kliknij „Run”.
   - Alternatywnie z wiersza poleceń:
     ```bash
     mvn javafx:run
     ```
   - Po uruchomieniu pojawi się ekran **SplashScreen**, następnie ekran **logowania**.

---

## Plan rozwoju

1. **Integracja z rzeczywistą bazą danych** (MySQL / PostgreSQL).
2. **Dodanie testów jednostkowych** i integracyjnych.
3. **Rozszerzenie modułu raportowania** o dodatkowe statystyki i wykresy.
4. **Usprawnienia interfejsu użytkownika** (styling CSS, responsywność).

---

## Wkład (Contributors)

- **Dawid** – [frontend, JavaFX]
- **Piotr** – [backend, logika biznesowa]
- **Łukasz** – [baza danych, projekt DB]
- **Piotr** – [baza danych, migracje, optymalizacja]

---

## Licencja

> **Uwaga**: Projekt jest w ciągłym rozwoju w ramach Programowania Zespołowego.
> Część modułów może nie być w pełni ukończona lub wymagać dodatkowych testów.

---
