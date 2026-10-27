# COD-90 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Epic: COD-90 — Owner search improvements
- Scope: preserve search params across owner pagination; add City filter; configurable page size via `petclinic.owners.pageSize`.
- Out of scope: new filters beyond City; user-selectable page size.
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/29786113/FRD+-+COD-90+-+Owner+search+improvements
- Jira Epic: https://manoj16239.atlassian.net/browse/COD-90

## 2) Delivery order (blockers first)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 2 | - |
| 2 | COD-49 | Make Owner search results page size configurable | 3 | COD-80 |
| 3 | COD-48 | Add city filter to Owner search (first slice) | 3 | COD-80 |

## 3) Story plans

### COD-80
- AC recap: paging retains `lastName` when filtered; unfiltered paging omits it; whitespace trimmed value persists.
- Affected files:
  - UI: `src/main/resources/templates/owners/ownersList.html`
  - Controller (model attrs for templating): `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - Tests: `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java` (or new MVC test)
- Sub-tasks by layer:
  - UI: adjust all paging links (page numbers + first/prev/next/last) to include `lastName` iff non-empty.
  - Controller: ensure model exposes the effective (trimmed) `lastName` used for query (e.g., `searchLastName`).
  - Test: add MockMvc assertions that paging links include `lastName=Fra` after a filtered search; ensure empty search does not add param.
- Tests per AC: MVC tests validating link HREFs + controller behavior across `page` with/without `lastName`.

### COD-49
- AC recap: default page size 5; config `petclinic.owners.pageSize=10` honored; invalid/non-positive falls back to 5.
- Affected files:
  - Controller: `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - Config (optional): `src/main/resources/application.properties` (only if needed for defaults/doc)
  - Tests: `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeTests.java`
- Sub-tasks by layer:
  - API: inject pageSize via `@Value` or `@ConfigurationProperties` with default 5; guard for non-positive.
  - Controller: use injected pageSize in PageRequest creation; keep external page param 1-based.
  - Test: ensure existing page-size test passes and add coverage for invalid values fallback.
- Tests per AC: MVC test(s) verifying `Page#getTotalPages()` under configured/invalid settings.

### COD-48
- AC recap: search supports optional City + lastName; no-results shows existing notFound; paging preserves both params.
- Affected files:
  - Domain: `src/main/java/org/springframework/samples/petclinic/owner/Owner.java` (must already contain `city`; use existing field)
  - UI: `src/main/resources/templates/owners/findOwners.html`, `src/main/resources/templates/owners/ownersList.html`
  - Controller: `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - Repository: `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - Tests: `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: add City input to findOwners form bound to owner.city; show active filters on results (optional banner).
  - API/Repo: add repository method (derived query or @Query) matching BRs: lastName prefix + optional city constraint.
  - Controller: trim/normalize city like lastName; route to correct repository call depending on blank/non-blank fields.
  - UI paging: extend COD-80 link pattern to include `city` as well when present.
  - Test: add MockMvc tests for (a) combined filters return list view, (b) no results returns findOwners with error, (c) paging preserves both params.
- Tests per AC: MVC tests for search combinations and pagination link generation with both params.

## 4) Branch & commit strategy
- Plan branch: `plan/COD-90-implementation-plan` (this PR only contains the plan).
- Implementation branch (later): `feature/COD-90-owner-search`.
- Commit plan for implementation (one commit per Story):
  - `COD-80: preserve lastName across owner pagination`
  - `COD-49: make owners page size configurable`
  - `COD-48: add city filter to owner search`
- One implementation PR for the Epic; keep commits Story-scoped for easy review/revert.

## 5) Build, local run, health check
- Build/tests: `./mvnw test` (repo also has Gradle; prefer Maven per baseline).
- Run: `./mvnw spring-boot:run` then browse `http://localhost:8080/`.
- Health: open Find Owners, search with `lastName=Fra`, page to 2; verify URL retains params; verify `/actuator/health` if actuator enabled (Assumption).

## 6) Risks
- Risk: Thymeleaf URL building with optional params can produce malformed links; mitigate with helper model attrs + conditional fragments (Owner: Frontend dev).
- Risk: Repository query for optional city can explode into multiple methods or unintended matching semantics; mitigate by choosing explicit @Query or clear derived method naming + tests (Owner: Backend dev).
- Risk: Config injection for page size may break existing tests/contexts if not defaulted properly; mitigate with safe default and validation (Owner: Tech lead).

## 7) Definition of Done / approval gates
- DoD: all ACs met; unit/MVC tests passing; no regressions in Owner search flows; code reviewed.
- Gates: Plan PR approved → (optional) Design note in Confluence → Code PR (feature/COD-90-owner-search) → Review + CI green → UAT on local → Docs/notes updated.

## 8) Assumptions & open questions
- Assumption: `Owner` already has `city` field (expected in PetClinic); no schema changes required.
- Open question: City matching semantics (exact vs contains, case sensitivity) — confirm with PO; implement consistently with existing patterns.
- Open question: Should results page show a “Filtered by …” banner (FRD notes optional); decide before COD-48 UI work.
