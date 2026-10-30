# COD-115 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- **Epic:** COD-115 — Owner search improvements
- **Scope:** (1) preserve active filters in Owner results pagination, (2) add optional City filter to Owner search + pagination, (3) externalize owners results page size.
- **Out of scope:** user-selectable page size UI, extra filters (firstName/telephone), sorting changes.
- **Primary modules:** Owner MVC flow: Find Owners → results list with pagination.
- **Repo baseline (verified):** `OwnerController`, `OwnerRepository`, `owners/findOwners.html`, `owners/ownersList.html`, existing MVC tests.
- **FRD (Confluence):** https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/30965761/FRD+-+COD-115+-+Owner+search+improvements
- **Jira Epic:** https://manoj16239.atlassian.net/browse/COD-115

## 2) Delivery order (by dependencies)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 1 | — |
| 2 | COD-98 | Make Owner search results page size configurable | 1 | COD-80 |
| 3 | COD-48 | Add city filter to Owner search (first slice) | 2 | COD-80 |

## 3) Story plans

### COD-80
- **AC recap:** Paging links retain `lastName` when present; unfiltered paging doesn’t add params; whitespace-trimmed value used on later pages.
- **Affected files (verified):**
  - `src/main/resources/templates/owners/ownersList.html` (pagination links)
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java` (ensure model exposes active filter; confirm trimmed value)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java` (add/adjust MVC tests)
- **Sub-tasks by layer:**
  - UI (Thymeleaf): update first/prev/next/last + numbered page links to include `lastName` only when present.
  - Controller: ensure `addPaginationModel(...)` (or equivalent) adds active `lastName` to the model for URL building.
  - Tests: add MockMvc assertion(s) to verify model contains trimmed `lastName` and/or that rendered HTML contains `lastName=` in pagination hrefs.
- **Test types per AC:**
  - AC-1: MVC test that GET `/owners?lastName=Fra&page=2` returns list constrained to “Fra” (existing fixture data) and rendered pagination includes `lastName=Fra`.
  - AC-2: MVC test that GET `/owners?page=2` renders pagination hrefs without `lastName=`.
  - AC-3: MVC test that GET `/owners?lastName=%20%20Fra%20%20` produces pagination links using `Fra` (trimmed).

### COD-98
- **AC recap:** default page size=5; configurable via `petclinic.owners.pageSize`; invalid/non-positive falls back to 5.
- **Affected files (verified):**
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java` (replace hard-coded `pageSize = 5`)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java` (already exists; extend/confirm)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java` (optional: add config-based test)
  - `src/main/resources/application.properties` (or relevant config file) (Assumption: exists; verify before change)
- **Sub-tasks by layer:**
  - Config: introduce property `petclinic.owners.pageSize` with default 5 (e.g., `@Value` or `@ConfigurationProperties`).
  - Controller: use effective page size when building `PageRequest`; ensure fallback on invalid values.
  - Tests: add a test profile/property override to validate AC-2; ensure invalid-config test covers AC-3.
- **Test types per AC:**
  - AC-1: MVC test without property override expects 5/page (assert `page.totalPages` / `page.size` via model).
  - AC-2: MVC test with `petclinic.owners.pageSize=10` expects size 10 and adjusted totalPages.
  - AC-3: unit/MVC test class already present for invalid config; ensure it asserts fallback behavior.

### COD-48
- **AC recap:** Add optional City filter; combined filter (`lastName` prefix + city match) works; no-results stays on Find Owners with existing notFound behavior; paging preserves both params.
- **Affected files (verified + assumptions noted):**
  - `src/main/resources/templates/owners/findOwners.html` (add City input)
  - `src/main/resources/templates/owners/ownersList.html` (show active filters; paging links include `lastName` + `city`)
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java` (parse/trim city; include in redirects + pagination model)
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java` (Assumption: add new repository method/query supporting city + lastName)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java` (add MVC tests for city filtering + paging)
- **Sub-tasks by layer:**
  - UI: add City field to Find Owners form; optionally echo active criteria on results.
  - Controller: accept `city` as request parameter and normalize (trim); pass to repository; ensure paging links preserve `city`.
  - Repository: implement query method for combined filtering with pageable.
  - Tests: add MVC tests for (a) combined filters, (b) no-results returns findOwners with error, (c) pagination retains both params.
- **Test types per AC:**
  - AC-1: MVC test that `/owners?lastName=Davis&city=Madison` returns only matching owners (verify via model attributes / view content).
  - AC-2: MVC test that unmatched city keeps user on `owners/findOwners` with `lastName` field error (existing behavior).
  - AC-3: MVC test that pagination links include both `lastName` and `city` when present.

## 4) Branch & commit strategy
- **Plan PR branch (this doc):** `plan/COD-115-implementation-plan` (one commit: `COD-115: add implementation plan`).
- **Implementation branch strategy (for dev team):** `feature/COD-115-owner-search`.
- **Commits:** one commit per Story, prefixed with the Story key (e.g., `COD-80: preserve lastName in paging links`).
- **PRs:** single implementation PR for COD-115; keep Story commits separate for easier review/reverts.

## 5) Build, local deployment, and health check
- `mvn -q clean test` (runs unit + MVC tests).
- `mvn spring-boot:run` then validate:
  - `/owners/find` renders Find Owners form.
  - Search `lastName=Fra` → results list; paginate and confirm filter preserved.
  - Search with City after COD-48: `lastName=Davis&city=Madison`.
- Basic smoke: app starts on default port and renders home page; no template errors in logs.

## 6) Risks
- **Risk 1 (Owner: Tech Lead):** City filter semantics (exact vs case-insensitive vs prefix) unclear; could cause test brittleness across DB collation.
- **Risk 2 (Owner: QA):** Pagination link assertions can be fragile to template formatting; prefer robust HTML matchers or model-based assertions.
- **Risk 3 (Owner: Dev):** Adding new repository query may impact performance; ensure proper indexing assumptions and keep query simple.

## 7) Definition of Done & approval gates
- Plan PR approved by Tech Lead.
- Design/FRD confirmed in Confluence (COD-115 FRD link above).
- Code PR: passes `mvn test`, includes/updates MVC tests per AC.
- Peer review completed; QA validates paging + filters manually.
- Release notes / brief docs update if property `petclinic.owners.pageSize` is introduced.

## 8) Assumptions & open questions
- **Assumption (to verify in implementation):** `src/main/resources/application.properties` is the correct place to document the new page-size property.
- **Open question:** Should City matching be case-insensitive and/or prefix-based? (FRD lists as open question.)
- **Open question:** Should empty-result messaging improvements be included in COD-115? Epic description mentions it; Stories covered here do not include a dedicated empty-message story.
