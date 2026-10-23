# COD-46 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: improve Owner search UX by (1) preserving criteria across pagination, (2) adding City filter slice, and (3) making page size configurable.
- In scope: `OwnerController` search flow, `OwnerRepository` query support, Thymeleaf templates `findOwners` and `ownersList`, controller tests.
- Out of scope: owner create/edit/details flows; non-owner modules; telephone/firstName filters; sorting changes (unless required for correctness).
- Epic: https://manoj16239.atlassian.net/browse/COD-46
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/27394050/FRD+-+COD-46+-+Owner+search+improvements

## 2) Delivery order (respecting blockers)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-47 | Preserve owner last-name filter in pagination links | 1 | - |
| 2 | COD-48 | Add city filter to Owner search (first slice) | (not set) | COD-47 |
| 3 | COD-49 | Make Owner search page size configurable | (not set) | COD-47 |

## 3) Story plans

### COD-47
- AC recap: pagination links (first/prev/next/last + page numbers) keep `lastName`; out-of-range redirect keeps `lastName`.
- Affected files:
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: add `lastName` as query param in all paging URLs (likely via `@{/owners(...)} ` syntax) and ensure links use the same binding name used by the controller.
  - Controller: ensure model contains the active `lastName` value for rendering (e.g., set `model.addAttribute("lastName", lastName)` alongside pagination attrs) and keep redirect attributes consistent.
  - Tests: extend MVC tests to validate generated paging links include `lastName` (and that repository call is still made with the same criteria).
- Tests per AC:
  - AC-1/2: controller/view test asserting HTML contains page links with `lastName=`.
  - AC-3: existing redirect test should be updated/confirmed to include `lastName` in redirect URL.

### COD-48
- AC recap: add City input; trim/blank treated as not provided; paging preserves both `lastName` and `city`.
- Affected files:
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: add City field (bind to `Owner.city`); ensure form submits `city` and results page keeps it in pagination links.
  - Controller: read/trim `city`; define “not provided” rule; pass both criteria to repository query.
  - Repository: add a query method supporting `lastName` + `city` (semantics per FRD open question; implement as starts-with or contains). Maintain existing lastName-only path when city absent to avoid behavior regressions.
  - Tests: add MVC tests for city filtering + whitespace handling; add paging link assertions include both params.
- Tests per AC:
  - AC-1: MVC test verifying repository called with expected criteria when city is provided.
  - AC-2: MVC test verifying whitespace city does not affect behavior (falls back to lastName-only query).
  - AC-3: MVC test verifying generated paging links include both `lastName` and `city`.

### COD-49
- AC recap: page size comes from config (not hard-coded 5); invalid config falls back safely.
- Affected files (expected):
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/resources/application.properties` (or `application.yml`) (Assumption)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - Config: introduce property key (e.g., `petclinic.owner.search.pageSize`) and read it via `@Value` or `@ConfigurationProperties`.
  - Controller: replace `int pageSize = 5` with configured value; validate positive; apply default when invalid/missing.
  - Tests: add/adjust test context to supply property override; assert Pageable size passed to repository equals configured value; add a negative/zero property case if feasible.
- Tests per AC:
  - AC-1/2: MVC test verifying `PageRequest` size equals configured property.
  - AC-3: MVC test verifying fallback behavior when property invalid.

## 4) Branch and commit strategy
- Plan branch (this PR): `plan/COD-46-implementation-plan`.
- Implementation branch (for dev work): `feature/COD-46-owner-search`.
- Commits: one commit per Story, prefixed with story key (e.g., `COD-47: preserve criteria in paging links`).
- PRs: one code PR for the Epic (can be merged story-by-story via commits), referencing this plan PR.

## 5) Build / run / health check
- Build: `./mvnw -q -DskipTests=false test`.
- Run locally: `./mvnw spring-boot:run` then open `http://localhost:8080`.
- Manual check: Owners → Find Owners; search `lastName=...` then page Next/Prev; verify query params remain; verify City filter once implemented.

## 6) Risks
- Risk: URL param names mismatch between form binding and pagination links (Owner model vs explicit params). Owner: Tech Lead.
- Risk: City match semantics ambiguity (starts-with vs contains, case sensitivity) could cause failing expectations. Owner: Product Owner.
- Risk: Adding new repository query method may change behavior when lastName blank (broad search) if not carefully handled. Owner: Developer.

## 7) Definition of Done & approval gates
- DoD: ACs met; MVC tests updated/added; `mvn test` green; no regressions in owner flows.
- Gates: Plan PR approved → (optional) design note in Confluence → Code PR opened → Code review → CI tests pass → Merge → Docs updated (if new config property).

## 8) Assumptions & open questions
- Confirm City matching rule (starts-with vs contains; case sensitivity) before implementing COD-48.
- Decide property key name and whether size can be overridden by request param (FRD notes optional); plan assumes configuration-only unless clarified.
- Assumption: config lives in `src/main/resources/application.properties` (verify in repo).
