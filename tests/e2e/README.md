# E2E tests (Playwright)

These end-to-end tests cover the Owner search/pagination enhancements for epic **COD-131**.

## Prerequisites

- Node.js 18+
- Java 17
- Maven wrapper (`./mvnw`)

## Install

From repo root:

```bash
cd tests/e2e
npm install
npx playwright install --with-deps
```

## Start the application

From repo root:

```bash
./mvnw spring-boot:run
```

By default tests use `http://localhost:8080`. Override with:

```bash
BASE_URL=http://localhost:8080 npx playwright test
```

## Run tests

```bash
cd tests/e2e
npx playwright test
```

## Traceability (AC → scenario/test)

| Story | AC | Gherkin scenario | Playwright test title |
|------|----|-------------------|-----------------------|
| COD-80 | AC-1 | Page-number link preserves lastName filter | COD-80 AC-1 page-number link preserves lastName filter |
| COD-80 | AC-2 | Unfiltered pagination does not add lastName parameter | COD-80 AC-2 unfiltered pagination does not add lastName parameter |
| COD-80 | AC-3 | Whitespace in lastName is trimmed and preserved in links | COD-80 AC-3 whitespace trimmed lastName is preserved |
| COD-80 | Edge | All pagination controls preserve active filters | COD-80 Edge all pagination controls preserve active filters |
| COD-98 | AC-1 | Default owner page size is 5 when not configured | COD-98 AC-1 default page size is 5 |
| COD-98 | AC-2 | Configured owner page size is applied | COD-98 AC-2 configured page size is applied (assumption) |
| COD-98 | AC-3 | Non-positive configured page size falls back to 5 | COD-98 AC-3 invalid page size falls back to default (assumption) |
| COD-48 | AC-1 | City filter restricts results along with lastName | COD-48 AC-1 city + lastName filter restricts results (assumption data) |
| COD-48 | AC-2 | City with no matches shows notFound behavior | COD-48 AC-2 no results stays on find page and shows notFound |
| COD-48 | AC-3 | Paging preserves both lastName and city | COD-48 AC-3 paging preserves lastName and city |
| COD-48 | Edge | City blank does not restrict results | COD-48 Edge blank city does not add city param |

Notes:
- Items marked **assumption** depend on seeded data / runtime config and are coded defensively, but should be validated by QA.
- Tests are **not executed** by this assistant.
