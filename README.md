# INF_ROZOWI (Programowanie zespołowe)

## Opis projektu

INF_ROZOWI to system zarządzania zadaniami dedykowany firmom informatycznym, którego celem jest usprawnienie planowania, monitorowania i raportowania zadań oraz projektów. System wspiera współpracę zespołu poprzez klarowny podział ról – Administratora, Kierownika i Pracownika – zapewniając odpowiedni poziom dostępu i funkcjonalności dla każdej z grup. Dzięki niemu możliwe jest efektywne śledzenie historii zmian w kodzie, zarządzanie backupami oraz precyzyjna kontrola nad procesem wytwarzania oprogramowania.

## Kluczowe funkcjonalności

- **Moduł administracji użytkownikami**: Tworzenie, edycja, usuwanie kont i przypisywanie ról, co umożliwia zarządzanie uprawnieniami w systemie.
- **Moduł zarządzania zadaniami**: Umożliwia tworzenie, edytowanie, usuwanie oraz przypisywanie zadań, a także monitorowanie postępów prac.
- **Moduł raportowania**: Generowanie raportów PDF dotyczących postępów zadań, projektów i wydajności użytkowników, z możliwością filtrowania według kluczowych kryteriów.
- **Moduł konfiguracji**: Dostosowanie ustawień systemowych, w tym definiowanie nowych ról, priorytetów i statusów zadań.

## Architektura systemu

Projekt opiera się na architekturze warstwowej, dzielącej się na:

- **Frontend**: Interfejs użytkownika (wstępnie opracowany w JavaFX), zapewniający intuicyjną obsługę systemu.
- **Backend**: Logika biznesowa, odpowiedzialna za przetwarzanie danych i komunikację z bazą danych.
- **Baza Danych**: Magazynowanie informacji o użytkownikach, zadaniach, projektach oraz raportach.

## Instalacja i uruchomienie

1. Sklonuj repozytorium:
   ```bash
   git clone https://github.com/PZ-2025/INF_ROZOWI
   ```
