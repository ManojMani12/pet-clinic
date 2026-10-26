# COD-46 Implementation Plan — Owner search improvements

## 1) Summary / Scope / Links
- Goal: improve Owner search results by preserving criteria across pagination, adding city filter, and adding results context + pageSize.
- In scope: `/owners/find`, `/owners` search handler, `ownersList.html` paging links and criteria display, repository query updates, MVC tests.
- Out of scope: other modules (pets, visits), sorting, fuzzy search/autocomplete.
- Epic: https://manoj16239.atlassian.net/browse/COD-46
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/27394050

## 2) Delivery order (respecting blockers)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-28 | Preserve lastName filter across Owner search pagination | 2 | - |
| 2 | COD-31 | Add city filter to Owner search | 3 | COD-28 |
| 3 | COD-32 | Improve Owner search pagination context and controls | 3 | COD-28 |

## 3) Story plans

### COD-28
- AC recap: paging links keep `lastName` when present; when unfiltered, no empty `lastName` param; first/last also preserve filter.
- Affected files: `src/main/resources/templates/owners/ownersList.html`, `src/main/java/.../owner/OwnerController.java`, `src/test/java/.../owner/OwnerControllerTests.java`.
- UI: update all paging `th:href` to include `lastName` only when non-blank (e.g., conditional param assembly).
- Controller: ensure model includes current `lastName` for list view (so template can render links); keep existing redirect out-of-range behavior.
- Tests: MVC tests for `/owners?page=2&lastName=...` staying filtered and model includes `lastName`; verify page links rendered with `lastName` when set.

### COD-31
- AC recap: add optional `city` filter (alone or with lastName); no-results shows error on find view; paging preserves `city` (+ `lastName`).
- Affected files: `src/main/resources/templates/owners/findOwners.html`, `src/main/resources/templates/owners/ownersList.html`, `src/main/java/.../owner/OwnerController.java`, `src/main/java/.../owner/OwnerRepository.java`, `src/test/java/.../owner/OwnerControllerTests.java`.
- UI: add `city` input bound to Owner (or separate request param) and submit via GET; ensure it round-trips in list paging links.
- Repo: add a query method supporting city + lastName (AND). City matching semantics TBD (open question); implement consistent with decision.
- Controller: normalize both params (trim; treat null/blank as empty); choose repository method based on which fields present.
- Tests: MVC tests for city-only, lastName+city, and no-results city; paging links include `city` (and lastName when set).

### COD-32
- AC recap: results page displays applied criteria; supports `pageSize` param (valid range), persists across paging; invalid pageSize falls back to default 5.
- Affected files: `src/main/resources/templates/owners/ownersList.html`, `src/main/java/.../owner/OwnerController.java`, (optional) `src/main/resources/application.properties`, `src/test/java/.../owner/OwnerControllerTests.java`.
- Controller: accept `pageSize` request param; validate numeric + bounds (assumption: allowed {5,10,20} or min/max); apply to PageRequest; fall back to 5 on invalid.
- UI: show criteria summary (e.g., lastName/city or “All owners”); add page-size selector that submits with criteria; ensure all paging links preserve `pageSize`.
- Tests: unit/MVC tests for pageSize affects PageRequest, invalid pageSize defaults, and rendered links keep `pageSize` + criteria.

## 4) Branch / commit strategy
- Plan branch: `plan/COD-46-implementation-plan` (this PR only contains the plan file).
- Implementation branch: `feature/COD-46-owner-search`.
- Commits: one commit per Story, prefixed with key (e.g., `COD-28: preserve lastName in paging links`).
- PRs: one implementation PR for all three stories (unless review size requires split).

## 5) Build, local run, and health check
- Build/tests: `./mvnw test` (or `mvn test`).
- Run app: `./mvnw spring-boot:run`.
- Manual check: open `/owners/find`, search with `lastName=Franklin`, verify paging keeps filter; repeat with `city`; verify pageSize changes items per page.

## 6) Risks
- Risk (Tech Lead): City matching semantics not finalized (exact vs startsWith/contains) can cause rework; mitigate by confirming before repository method is chosen.
- Risk (QA): Thymeleaf URL composition errors may drop params on some links (first/last/prev/next); mitigate with template-based assertions in MVC tests.
- Risk (Dev): Adding `pageSize` can create edge cases with out-of-range pages; mitigate by reusing existing redirect-to-page-1 logic while preserving params.

## 7) Definition of Done & approval gates
- DoD: all ACs pass via automated tests; no regression in existing owner search flows; templates render correctly; PR reviewed and approved.
- Gates: Plan PR approved → (optional) design note in Confluence updated → Code PR opened → Review → CI green → Manual smoke (owners search) → Merge.

## 8) Assumptions / Open questions
- Open: City matching rule (exact vs startsWith vs contains; case sensitivity). FRD defaults to exact unless PO specifies otherwise.
- Assumption: When both `lastName` and `city` provided, filtering is AND.
- Assumption: pageSize allowed set/range will be defined (proposal: min 5, max 50, default 5).
