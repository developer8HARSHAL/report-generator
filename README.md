# Async Report Generator

![Java](https://img.shields.io/badge/Java-21-007396) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F) ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1) ![Build](https://img.shields.io/badge/build-passing-brightgreen)

Submit a report request → get a `jobId` instantly → poll for status → download when ready.

---

## How It Works

Instead of blocking the HTTP thread for a slow report, the API returns `202 Accepted` immediately. The report is generated in a background thread pool. The client polls for status and downloads the file when `DONE`.

---

## Architecture

```
[Client]
   │  POST /api/reports/request
   ▼
[ReportController]  ──→  saves job (QUEUED)  ──→  [MySQL]
   │  returns 202 + jobId
   ▼
[ReportService]  ──→  triggers @Async
   ▼
[ThreadPoolTaskExecutor]  core=5, max=10, queue=50
   │  status → PROCESSING → DONE (writes CSV to disk)
   └→ status → FAILED (on exception)

[Quartz Scheduler]  runs every minute
   └→ DONE jobs older than 24h → delete file → EXPIRED

[Client polls]
GET /api/reports/{jobId}/status  →  DONE
GET /api/reports/{jobId}/download  →  CSV file
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.3, Java 21 |
| Async | `@Async` + `ThreadPoolTaskExecutor` |
| Scheduler | Quartz |
| Database | MySQL 8 + Spring Data JPA + Flyway |
| File Generation | OpenCSV |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito |

---

## API Reference

### POST `/api/reports/request`
```json
// Request
{ "reportType": "SALES", "fromDate": "2024-01-01", "toDate": "2024-12-31", "requestedBy": "user@example.com" }

// 202 Accepted
{ "jobId": "a3f9d2c1-...", "status": "QUEUED", "message": "Poll /api/reports/a3f9d2c1-.../status" }
```

### GET `/api/reports/{jobId}/status`
```json
// 200 OK
{ "jobId": "a3f9d2c1-...", "status": "DONE", "completedAt": "2024-06-01T10:00:06" }
```

### GET `/api/reports/{jobId}/download`
```
200 OK        → CSV file stream
409 Conflict  → "Report not ready yet: PROCESSING"
410 Gone      → "Report has expired, please request a new one"
```

### GET `/api/reports?requestedBy=user@example.com`
```json
// 200 OK
[{ "jobId": "...", "reportType": "SALES", "status": "DONE" }]
```

---

## Job State Machine

```
[QUEUED] → [PROCESSING] → [DONE] → [EXPIRED]
                └→ [FAILED]
```

---

## Local Setup

**Prerequisites:** Java 21, Maven, MySQL 8

1. Clone the repo:
```bash
git clone https://github.com/your-username/async-report-generator.git
cd async-report-generator
```

2. Create the database:
```sql
CREATE DATABASE report_generator_db;
```

3. Set your DB password (default is `2003`):
```bash
export DB_PASSWORD=your_password   # Linux/macOS
set DB_PASSWORD=your_password      # Windows
```

4. Run — Flyway migrations execute automatically on startup:
```bash
./mvnw spring-boot:run
```

- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Running Tests

```bash
./mvnw test
```

Covers: job creation with `QUEUED` status, `ReportNotFoundException` for unknown IDs, status DTO mapping, and Spring context load.

---

## Project Structure

```
src/main/java/com/Harshal/report_generator/
├── config/        ← AsyncConfig, QuartzConfig, OpenApiConfig
├── controller/    ← ReportController
├── service/       ← ReportService, ReportGeneratorService, ReportCleanupJob
├── repository/    ← ReportJobRepository
├── model/         ← ReportJob, ReportStatus
├── dto/           ← ReportRequestDTO, ReportStatusDTO, ErrorResponseDTO
└── exception/     ← GlobalExceptionHandler, custom exceptions
```

---

## Key Design Decisions

- **`@Async` in a separate bean** — self-invocation bypasses Spring's proxy; calling from a different bean ensures the method actually runs on the thread pool.
- **Re-fetch job inside async method** — the passed object is detached after the original transaction closes; fetching fresh avoids stale-state bugs.
- **Quartz over `@Scheduled`** — supports clustering and survives restarts; production-safe for cleanup jobs.
