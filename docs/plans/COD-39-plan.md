# COD-39 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: improve Owner search by preserving filters across pagination, adding city filter, and improving pagination UX (range + page size + invalid page feedback).
- In scope: `GET /owners` find form + list page only; no changes to Owner CRUD flows.
- Out of scope: sorting, other entity lists, additional filters beyond city.
- Epic: https://manoj16239.atlassian.net/browse/COD-39
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/27164673/FRD+-+COD-39+-+Owner+search+improvements (Assumption: link is valid)

## 2) Delivery order (blockers first)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-28 | Preserve lastName filter across Owner search pagination | 1 | — |
| 2 | COD-31 | Add city filter to Owner search | 3 | COD-28 |
| 3 | COD-32 | Improve Owner search pagination context and controls | 3 | COD-28 |

## 3) Story plans

### COD-28
- AC recap: pagination links must retain `lastName` when active; no `lastName` param when none; whitespace-trimmed value persists.
- Affected files: 
  - `src/main/resources/templates/owners/ownersList.html`
  - (Potential) `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java` (ensure model exposes applied lastName if needed)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: update all paging links (first/prev/next/last and page numbers) to include `lastName=${lastName}` only when present.
  - Controller: verify model includes the applied lastName used for search (if not already; otherwise no change).
  - Tests: extend MockMvc tests to assert generated paging links include/omit `lastName` per AC, and trimming behavior.
- Tests per AC: 
  - AC-1: MVC test checks ownersList view contains links with `lastName=Fra` for navigation.
  - AC-2: MVC test checks links do not include `lastName` when search is unfiltered.
  - AC-3: MVC test calls `/owners?page=2&lastName=%20%20Fra%20%20` and asserts subsequent links use `Fra`.

### COD-31
- AC recap: add optional `city` filter ANDed with lastName prefix; no results shows existing not-found behavior; paging retains `lastName` + `city`.
- Affected files:
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: add a City input (query param `city`) on find form; ensure ownersList paging links include `city` when present.
  - Controller: read/trim city (similar to lastName); pass both filters to repository; keep existing 0/1/many branching.
  - Repository: add query method(s) supporting lastName prefix + city (AND) with paging.
  - Tests: add coverage for combined filtering, notFound behavior, and pagination param retention.
- Tests per AC:
  - AC-1: MVC test mocks repository to return owners only when both lastName + city match.
  - AC-2: MVC test expects findOwners view + `lastName` (or city) field error when no results.
  - AC-3: MVC test asserts ownersList links include both `lastName` and `city`.

### COD-32
- AC recap: show “Showing X–Y of Z”; support `pageSize` in {5,10,20} and persist in URL; invalid page redirects with info message and preserves params.
- Affected files:
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
  - (Potential) `src/main/resources/application.properties` (only if FRD requires configurable default; otherwise keep default=5)
- Sub-tasks by layer:
  - Controller: accept optional `pageSize`; validate allowed values; feed pageSize into PageRequest; compute range values for model; implement invalid page redirect preserving `lastName`/`city`/`pageSize` and add info flag/message.
  - UI: render range text using model values; add page size selector that submits as query param; ensure all paging links carry `pageSize` + active filters.
  - Tests: verify range text values, pageSize persistence, invalid-page redirect and feedback message rendering.
- Tests per AC:
  - AC-1: MVC test ensures model contains computed range and view renders expected text for page 2.
  - AC-2: MVC test ensures navigation links include `pageSize=10` and repository called with size 10.
  - AC-3: MVC test ensures out-of-bounds triggers redirect to valid page and includes informational message indicator.

## 4) Branch and commit strategy
- Plan branch (this PR): `plan/COD-39-implementation-plan`.
- Implementation branch (for dev work): `feature/COD-39-owner-search`.
- Commits: one commit per Story, prefixed with key (e.g., `COD-28: preserve lastName in paging links`).
- PRs: one implementation PR for all Stories once plan approved (main is protected).

## 5) Build / local deploy / health check
- Build: `./mvnw -q test` (or `./gradlew test` if Gradle is used in CI; repo contains both).
- Run: `./mvnw spring-boot:run`.
- Smoke checks: open `/owners/find`, search for `lastName=Fra`, paginate; verify `city` filter; verify pageSize selection and invalid page redirect message.

## 6) Risks
- Risk 1 (Owner: Tech Lead): Query param preservation can drift across templates as new filters are added; mitigate by building a single Thymeleaf URL expression reused for all pager links.
- Risk 2 (Owner: QA): MVC tests asserting link href strings can be brittle; mitigate by asserting presence of key query params rather than full string equality.
- Risk 3 (Owner: Dev): Repository query semantics for city (case sensitivity) may not match stakeholder expectations; mitigate by documenting and confirming semantics (see open questions).

## 7) Definition of Done / approval gates
- DoD: all ACs pass; unit/MVC tests added; no regressions in existing Owner flows; code reviewed; updated templates render correctly.
- Gates: Plan PR approved → (optional) design notes in Confluence → Implementation PR opened → Code review → CI green → Manual smoke test → Merge → Docs updated (if UI text added).

## 8) Assumptions / open questions
- Assumption: City match is exact after trimming; case-sensitivity follows existing DB collation (H2 default). Confirm if case-insensitive is required.
- Assumption: Invalid page feedback can be implemented as a query flag or flash attribute rendered on ownersList.
- Open Q: Should `pageSize` default remain hardcoded 5 or be configurable (a Jira subtask suggests configuration; verify against FRD).
