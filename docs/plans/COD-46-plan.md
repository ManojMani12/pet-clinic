# COD-46 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: Preserve owner search filter across paging, make page size configurable, and improve owners list paging UX.
- In scope: `/owners` find flow (OwnerController paging), `ownersList.html` pagination links and context text.
- Out of scope: new filters beyond lastName, sorting, global pagination refactor.
- Epic: COD-46 (Jira) | FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/28508162

## 2) Delivery order (blockers first)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-62 | Preserve last-name filter in owner results pagination | 3 | - |
| 2 | COD-65 | Make owner search page size configurable | 2 | COD-62 |
| 3 | COD-68 | Enhance owners list with filter context, range display, and refine-search link | 3 | COD-62 |

## 3) Story plans

### COD-62
- AC recap: paging links (page#/first/prev/next/last) must keep `lastName`; trimmed lastName must be used; 0-results stays on find page.
- Affected files (verified):
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - Controller: ensure effective `lastName` used for query is also added to model for ownersList rendering (e.g., `model.addAttribute("lastName", lastName)` alongside pagination attrs).
  - View (Thymeleaf): update all paging `th:href` to include `lastName` query param when present (use model attribute), keeping current 1-based page param.
  - Tests (MVC): add tests asserting ownersList pagination links contain `lastName=<value>` and that whitespace input is trimmed and preserved in links.
- Tests per AC:
  - AC-1/AC-3: `@WebMvcTest(OwnerController)` mock repo to return multi-page `Page<Owner>`; assert response content contains `/owners?page=2&lastName=Frank` (and trimmed `Fran`).
  - AC-2: existing notFound path test remains; ensure no ownersList rendered.

### COD-65
- AC recap: page size comes from config (N>0); default 5 when missing; invalid (<=0) falls back to 5.
- Affected files (verified/assumption):
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java` (verified: currently `int pageSize = 5`)
  - `src/main/resources/application.properties` (assumption: exists in repo)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - Config: introduce property (e.g., `petclinic.owners.page-size=5`) and wire into controller via `@Value` or `@ConfigurationProperties` (prefer properties class if already used; otherwise `@Value`).
  - Controller: replace hard-coded pageSize; guard invalid values (`<=0` -> 5) before creating `PageRequest`.
  - Tests: add a test with `@TestPropertySource` overriding page size, verifying model `listOwners` size equals N and totalPages matches; add test for invalid value falling back to 5.
- Tests per AC:
  - AC-1: property override with mocked repo returning `PageImpl` sized N; verify view model contains N entries.
  - AC-2/AC-3: run test context with missing/invalid property and verify paging behavior uses 5 (via model sizes/totalPages).

### COD-68
- AC recap: ownersList shows filter context (lastName or “All owners”); shows range “Showing A–B of totalItems” when totalItems>0.
- Affected files (verified):
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java` (extend)
- Sub-tasks by layer:
  - Controller: compute and add model attrs for context and range: `effectiveLastName`, `showAllOwners` flag or `filterLabel`, and `rangeFrom/rangeTo` derived from `currentPage`, page size, and `totalItems`.
  - View: render context line and range line; add “Refine search” link to `/owners/find` (Assumption: can pass `lastName` as query param for prefill, or rely on model on find page).
  - Tests: assert rendered HTML contains context text for non-blank and blank lastName; assert correct range string on page 1 and later page.
- Tests per AC:
  - AC-1/AC-2: mock Page totals and assert response contains expected label.
  - AC-3: for known total/page/pageSize, assert “Showing A–B of X”.

## 4) Branch & commit strategy
- Plan branch (this PR): `plan/COD-46-implementation-plan`.
- Implementation branch: `feature/COD-46-owner-search`.
- Commit discipline: one commit per Story, prefixed with key (e.g., `COD-62: preserve lastName across pagination`).
- Delivery: single code PR to `main` covering COD-62, COD-65, COD-68 in the delivery order.

## 5) Build / local run / health check
- Build: `mvn -q clean test` (Java 17).
- Run: `mvn spring-boot:run`.
- Manual check: open `/owners/find`, search "Frank", click paging controls and verify URL keeps `lastName`; verify context/range text; adjust `petclinic.owners.page-size` and re-check.

## 6) Risks
- Risk: Thymeleaf link building may double-encode or omit params if lastName is blank. Mitigation owner: Tech Lead (review template link strategy).
- Risk: Page size config invalid values causing unexpected paging. Mitigation owner: Developer (guard + tests for <=0/missing).

## 7) Definition of Done / approval gates
- DoD: all ACs met; unit/MVC tests added and passing; no regressions in owner search flow; documentation updated (properties usage).
- Gates: Plan PR approved → (optional) design note in Confluence → Code PR created → peer review → CI green → manual smoke test → merge.

## 8) Assumptions & open questions
- Assumption: `src/main/resources/application.properties` exists and is the right place for new property; otherwise use existing config mechanism.
- Open Q: Should pagination links include `lastName` when blank (e.g., omit param vs `lastName=`)? Current AC implies include when non-blank; implement omission for blank to keep clean URLs.
- Open Q: Do we want a max page size cap (e.g., 100)? Not required by FRD; can be follow-up.
