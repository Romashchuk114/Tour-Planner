# Tour Planner

Webbasierte Anwendung zur Planung und Verwaltung von Touren inkl. Tour-Logs.

**Git-Repository:** https://github.com/Romashchuk114/Tour-Planner

## Setup & Starten

### 1. Datenbank starten

```bash
npm run start:db
```

### 2. Umgebungsvariablen

Eine `.env`-Datei im Root-Verzeichnis anlegen:

```env
POSTGRES_DB=<db-name>
POSTGRES_USER=<db-user>
POSTGRES_PASSWORD=<db-password>
DB_PORT=<port>

JWT_SECRET=<min-256-bit-secret>
JWT_EXPIRATION_MS=<expiration-in-ms>

IMAGE_DIR=<pfad-zu-bildern>
```
In IntelliJ muss diese `.env`-Datei in der Run Configuration eingebunden werden:

1. **Run > Edit Configurations...** öffnen
2. Die Spring Boot Run Configuration auswählen (z.B. `BackendApplication`)
3. Im Feld **Environment variables** rechts auf das Ordner-Symbol klicken
4. Unten links auf das **+** Symbol klicken und **Import from file** wählen (oder den Punkt-Symbol)
5. Die `.env`-Datei aus dem Root-Verzeichnis auswählen
6. Mit **OK** bestätigen

> **Hinweis:** Die `.env`-Datei ist in `.gitignore` eingetragen und wird nicht committed.

### 3. Backend starten

In IntelliJ die `BackendApplication`-Klasse öffnen und über den grünen **Run**-Button starten (oder **Shift + F10**).

> Voraussetzung: Die `.env`-Datei muss wie in Schritt 2 beschrieben in der Run Configuration eingebunden sein.

Das Backend läuft auf `http://localhost:8080`.

### 4. Frontend starten

```bash
npm run start:frontend
```

Das Frontend läuft auf `http://localhost:4200`.

## Technologien

| Bereich | Technologie |
|---------|------------|
| Backend | Java 25, Spring Boot 4.0.3 |
| Frontend | Angular 20, TypeScript |
| Datenbank | PostgreSQL 17 (Docker) |
| ORM | JPA / Hibernate |
| Logging | Log4j2 (SLF4J) |
| Build | Maven (Backend), npm (Frontend) |

## Architektur

### Schichtenarchitektur (Backend)

```
Presentation Layer   -->  Controller, DTOs, Exception Handler
Service Layer        -->  Business Logic, Validation
Data Access Layer    -->  JPA Repositories
Model                -->  JPA Entities
```

### MVVM (Frontend)

```
View       -->  Component Templates (HTML)
ViewModel  -->  Component Classes (TypeScript)
Model      -->  Services, Interfaces
```

### Design Patterns

- **Repository Pattern** - Spring Data JPA Repositories
- **DTO Pattern** - Trennung von Entities und API-Objekten
- **Dependency Injection** - Spring IoC / Angular DI

## Voraussetzungen

- Java 25
- Node.js 20+
- Docker

## API-Endpunkte

| Methode | Pfad | Beschreibung |
|---------|------|-------------|
| POST | `/api/auth/register` | Registrierung |
| POST | `/api/auth/login` | Anmeldung |
| GET | `/api/tours` | Alle Touren des Users |
| POST | `/api/tours` | Tour erstellen |
| GET | `/api/tours/{id}` | Tour abrufen |
| PUT | `/api/tours/{id}` | Tour bearbeiten |
| DELETE | `/api/tours/{id}` | Tour löschen |
| POST | `/api/tours/{id}/image` | Bild hochladen |
| GET | `/api/tours/{id}/image` | Bild abrufen |
| DELETE | `/api/tours/{id}/image` | Bild löschen |
| GET | `/api/tours/{tourId}/logs` | Logs einer Tour |
| POST | `/api/tours/{tourId}/logs` | Log erstellen |
| PUT | `/api/tours/{tourId}/logs/{logId}` | Log bearbeiten |
| DELETE | `/api/tours/{tourId}/logs/{logId}` | Log löschen |