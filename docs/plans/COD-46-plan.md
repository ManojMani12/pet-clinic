# COD-46 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: Preserve owner search filters across pagination, add City filter, and make owner search page size configurable.
- In scope: OwnerController search + paging, OwnerRepository query additions, Thymeleaf templates for Find Owners + Owners List, MVC tests.
- Out of scope: Sorting, fuzzy matching, user-selectable page size UI.
- Epic: COD-46 https://manoj16239.atlassian.net/browse/COD-46
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/27394050/FRD+-+COD-46+-+Owner+search+improvements

## 2) Delivery order (by blockers)
| Order | Story | Title | Points | Blocked by |
|------:|-------|-------|-------:|------------|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 2 | — |
| 2 | COD-48 | Add city filter to Owner search (first slice) | 3 | COD-80 |
| 3 | COD-49 | Make Owner search results page size configurable | 1 | COD-80 |

## 3) Story plans

### COD-80
- AC recap: paging links include lastName; retry/no-results unchanged; whitespace trimmed value persists across pages.
- Affected files:
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java (or new focused test)
- Sub-tasks by layer:
  - UI: update all pagination anchors (first/prev/next/last/page numbers) to include lastName when present.
  - Controller: ensure model exposes normalized lastName used for the query (so UI can reuse it); avoid regressions for empty lastName.
  - Tests: add MVC test asserting that when requesting /owners?lastName=Fr&page=2 the model retains lastName and rendered links contain lastName=Fr.
- Tests per AC:
  - AC1: MVC test for filtered paging request remains filtered (status 200 + model attribute(s)).
  - AC2: existing tests for no owners found remain passing.
  - AC3: MVC test for trimming (submit with spaces -> rendered pagination uses trimmed value).

### COD-48
- AC recap: optional City + lastName prefix returns ANDed results; no-results stays on findOwners; trimmed city persists across pages.
- Affected files:
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI: add City input bound to Owner.city on findOwners; optionally show active criteria on results (Assumption: via subtasks COD-52).
  - Controller: normalize/trim city; route to correct repository method depending on whether city is blank.
  - Repository: add Spring Data method for AND query with paging (e.g., findByLastNameStartingWithAndCity(..., Pageable)).
  - UI paging: extend COD-80 behavior to also include city in pagination links when present.
  - Tests: MVC test for lastName+city returns ownersList; MVC test for lastName+unknown city returns findOwners with error.
- Tests per AC:
  - AC1: MVC test with lastName + city yields ownersList and model contains both filters.
  - AC2: MVC test yields findOwners + field error.
  - AC3: MVC test ensures trimmed city is used/preserved in paging.

### COD-49
- AC recap: property petclinic.owners.pageSize overrides default; absent => 5; invalid non-positive handled (open decision).
- Affected files:
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/resources/application.properties (or yaml) (Assumption: property location)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeTests.java
- Sub-tasks by layer:
  - Config: bind petclinic.owners.pageSize with default 5 (via @Value or @ConfigurationProperties).
  - Controller: use configured pageSize for PageRequest creation.
  - Validation: implement chosen behavior for <=0 (prefer fallback to 5 to satisfy existing AC draft).
  - Tests: ensure OwnerControllerPageSizeTests passes; add test for invalid value behavior if implemented.
- Tests per AC:
  - AC1: existing OwnerControllerPageSizeTests.
  - AC2: existing tests (or add) confirming default 5.
  - AC3: new test for non-positive property if behavior is fallback; otherwise document fail-fast and add context test.

## 4) Branch & commit strategy
- Plan branch (this PR): plan/COD-46-implementation-plan.
- Implementation branch (code): feature/COD-46-owner-search.
- Commits: one commit per Story (prefix with story key), e.g., "COD-80: preserve lastName on pagination".
- PRs: one code PR for the Epic (feature/COD-46-owner-search -> main) unless team requests split.

## 5) Build, local deploy, health check
- Build/tests: ./mvnw test
- Run app: ./mvnw spring-boot:run
- Verify:
  - Navigate to /owners/find, search lastName, page results; confirm query params persist.
  - Search with lastName + city; confirm results and paging.
  - Set petclinic.owners.pageSize and confirm paging size changes.

## 6) Risks
- Risk: Thymeleaf link construction may double-encode or drop params; Owner/Model binding may not expose city/lastName consistently. Owner: UI dev.
- Risk: Query semantics for city (case sensitivity) may differ across DBs; tests could be brittle. Owner: QA/Dev.
- Risk: Page size invalid-value handling could diverge from expectations (fallback vs fail-fast). Owner: Tech Lead.

## 7) Definition of Done & approval gates
- DoD: All ACs met; unit/MVC tests green; no regression in existing Owner flows; code reviewed; docs updated (FRD/notes).
- Gates: Plan PR approved (this plan); optional design note in Confluence; Code PR + review; CI green; manual smoke test; merge to main; close stories.

## 8) Assumptions & open questions
- Assumption: City filter uses equals match (not contains) per Story text; case sensitivity depends on DB collation.
- Assumption: application uses application.properties for new default property.
- Open question: For petclinic.owners.pageSize <= 0, confirm fallback-to-5 vs fail-fast; plan assumes fallback.
