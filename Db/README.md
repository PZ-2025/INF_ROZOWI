# Katalog `database`

W tym folderze przechowywane są wszelkie pliki i informacje związane z bazą danych w projekcie **INF_ROZOWI**.  
Może on zawierać m.in.:

- **Skrypty SQL** do tworzenia i inicjalizacji bazy danych (np. pliki `schema.sql`, `data.sql`).
- **Skrypty migracyjne** (np. Flyway, Liquibase) umożliwiające stopniowe wprowadzanie zmian w strukturze bazy.
- **Dodatkowe dokumenty konfiguracyjne** zawierające parametry połączenia, nazwy tabel, hasła, itp. _(uwaga na przechowywanie wrażliwych danych w repozytorium)_.
- **Instrukcje uruchomienia bazy** w środowiskach lokalnych i produkcyjnych (np. Docker Compose, pliki `.env`).
