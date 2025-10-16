# Books API

A Spring Boot 3 (Java 21) REST service that loads a nested JSON book catalog from a remote URL with automatic classpath fallback, and exposes search endpoints (single keyword and multi-keyword ANY/ALL matching).

## Contents
- [Features](#features)
- [Architecture Overview](#architecture-overview)
- [Data Loading & Fallback](#data-loading--fallback)
- [Domain Model](#domain-model)
- [REST Endpoints](#rest-endpoints)
- [Search Semantics](#search-semantics)
- [Configuration](#configuration)
- [Build & Run](#build--run)
- [Testing](#testing)
- [Postman Collection](#postman-collection)
- [Examples](#examples)
- [Project Structure](#project-structure)
- [Extending / Next Steps](#extending--next-steps)

## Features
- Fetches remote JSON (books list) on first access (`books.remote.url`).
- Graceful fallback to bundled `classpath:books.json` if remote fetch fails.
- In‑memory cache (lazy load, reused for subsequent calls) with manual reload endpoint.
- Flexible search:
  - Simple single keyword via query parameter.
  - Multi-keyword POST body with ANY or ALL matching modes.
- Case-insensitive matching across multiple fields (title, subtitle, author, description, publisher, ISBN).
- Simple health-like reload endpoint returning metadata.
- Tests covering fallback and search behavior.

## Architecture Overview
```
Controller (BooksController)
  -> Service (BooksService)
       -> Remote fetch (RestClient) OR classpath fallback
       -> Parse JSON (Jackson) into List<Book>
       -> Cache in-memory
```

## Data Loading & Fallback
1. First search invocation triggers `BooksService.loadBooks()`.
2. Attempts HTTP GET to `books.remote.url`.
3. On any error (network, non-2xx, parse) it loads `books.json` from the classpath.
4. Parsed list cached for the lifetime of the JVM (until restart). Use `/api/books/reload` to force a reload attempt (cache is re-used if already populated; you can adjust if you want a hard refresh logic).

## Domain Model
`Book` fields: `isbn`, `title`, `subtitle`, `author`, `published`, `publisher`, `pages`, `description`, `website` (unknown fields ignored).

## REST Endpoints
Base path: `/api/books`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/books?query={keyword}` | Search by single keyword; omit `query` to list all. |
| POST | `/api/books/search` | Multi-keyword search with ANY/ALL mode. |
| GET | `/api/books/reload` | Trigger (lazy) load and return cache metadata. |

### Request Body (POST /api/books/search)
```json
{
  "keywords": ["java", "press"],
  "matchMode": "ANY"  // or "ALL" (default ANY if omitted / unknown)
}
```

### Response (search endpoints)
JSON array of `Book` objects.

### Reload Response
```json
{
  "count": 10,
  "lastLoaded": "2025-10-15T06:31:42.123Z"
}
```

## Search Semantics
- Fields scanned: title, subtitle, author, description, publisher, isbn.
- Case-insensitive substring match.
- ANY: at least one keyword matches one or more fields.
- ALL: every keyword must appear (in any fields, not necessarily the same field).
- Empty or missing keyword(s) returns full dataset.

## Configuration
Property (in `application.properties`):
```
books.remote.url=https://gist.githubusercontent.com/nanotaboada/6396437/raw/855dd84436be2c86e192abae2ac605743fc3a127/books.json
```
Override at runtime (examples):
```
# PowerShell
$Env:BOOKS_REMOTE_URL="https://example.com/alt-books.json"; mvnw.cmd spring-boot:run
```
Or via JVM system property:
```
set BOOKS_REMOTE_URL=https://example.com/alt-books.json
mvnw.cmd spring-boot:run -Dbooks.remote.url=%BOOKS_REMOTE_URL%
```

## Build & Run
```
# Package
mvnw.cmd package

# Run
mvnw.cmd spring-boot:run

# Or run the jar
java -jar target/Books-0.0.1-SNAPSHOT.jar
```
The service listens (by default) on `http://localhost:8080`.

## Testing
```
mvnw.cmd test
```
Key tests:
- `BooksServiceTest` – validates fallback + keyword search.
- `BooksControllerTest` – validates GET & POST endpoints.

## Postman Collection
Files:
- `postman_collection.json`
- `postman_environment.json`

Steps:
1. Import both files into Postman.
2. Select environment "Books API Local".
3. Start application.
4. Run requests sequentially (Reload -> List -> Searches).

Sample collection tests you can add (in Postman request Tests tab):
```javascript
pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("Array returned", () => pm.expect(Array.isArray(pm.response.json())).to.be.true);
```

## Examples
```
# All books
curl http://localhost:8080/api/books

# Single keyword
curl http://localhost:8080/api/books?query=javascript

# Multi-keyword ANY
curl -H "Content-Type: application/json" -d '{"keywords":["modern","press"],"matchMode":"ANY"}' \
  http://localhost:8080/api/books/search

# Multi-keyword ALL
curl -H "Content-Type: application/json" -d '{"keywords":["javascript","introduction"],"matchMode":"ALL"}' \
  http://localhost:8080/api/books/search
```

## Project Structure
```
src/
  main/
    java/com/example/cognizant/Books/
      BooksApplication.java
      controller/BooksController.java
      service/BooksService.java
      model/Book.java
    resources/
      application.properties
      books.json (fallback)
  test/
    java/com/example/cognizant/Books/
      BooksApplicationTests.java
      controller/BooksControllerTest.java
      service/BooksServiceTest.java
postman_collection.json
postman_environment.json
README.md
HELP.md
```

## Extending / Next Steps
- Pagination & sorting (e.g., by published date or title).
- ETag / conditional GET for client-side caching.
- Scheduled refresh or cache TTL invalidation.
- Add OpenAPI/Swagger via `springdoc-openapi` dependency.
- Validation & error responses for malformed POST bodies.
- Metrics & tracing integration (Micrometer / OpenTelemetry).
- Containerization (Dockerfile + CI pipeline).

## Using GitHub Actions (CI)

This project includes a GitHub Actions workflow that builds and tests the project automatically.

How to run it:

1. Push a commit to the `main` branch or open a pull request — the workflow will run automatically.
2. Or trigger it manually from the GitHub UI: go to the "Actions" tab, select the workflow named `CI`, then click "Run workflow".

What the workflow does:

- Checks out your repository
- Sets up Java 21
- Uses the Maven wrapper (`mvnw` / `mvnw.cmd`) to build and run tests
- Caches Maven dependencies to speed up subsequent runs

How to view results:

1. On GitHub, open the "Actions" tab and choose the most recent run. Steps and logs are visible there.
2. If a step fails, expand it to see the console output. The error message usually points to the failing test or build issue.

Optional: GitHub Badge

You can add a status badge to this README to show the build status. After the workflow runs, copy the badge from the workflow page and paste it at the top of this file.

If you'd like, I can add the badge automatically once you confirm the workflow runs on GitHub. 

---
Feel free to adapt or extend. PRs / improvements welcome.
