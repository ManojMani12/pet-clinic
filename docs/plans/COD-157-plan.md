# COD-157 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: improve Owner search by preserving search context across pagination, showing active criteria on results, and adding City filter.
- In scope: OwnerController + OwnerRepository + Thymeleaf templates under `templates/owners`, plus controller/view tests.
- Out of scope: changing lastName matching (still prefix); adding filters beyond City; changing page size.
- Epic: https://manoj16239.atlassian.net/browse/COD-157
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/33193985/FRD+-+COD-157+-+Owner+search+improvements

## 2) Delivery order
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-158 | Preserve last name filter across owner search pagination | 2 | — |
| 2 | COD-161 | Display active owner search criteria and provide back/clear actions | 1 | COD-158 |
| 3 | COD-164 | Add city filter to owner search (first slice) and preserve it in paging | 3 | COD-158 |

## 3) Story plans

### COD-158
- AC recap: pagination links include `lastName` when present; paging retains filtered set; out-of-bounds redirect to page=1 preserves `lastName`.
- Affected files (verified):
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/resources/templates/owners/ownersList.html`
  - Tests: `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - Controller: ensure normalized `lastName` is always added to model when returning `owners/ownersList` (so template can read it).
  - View: update all pagination URLs (first/prev/next/last + page numbers) to conditionally append `&lastName=${lastName}` when non-empty.
  - Tests: add MockMvc assertions that rendered HTML contains page links with `lastName=...`; cover next/prev and page-number links.
- Test types per AC:
  - AC-1: controller/view integration test verifying hrefs contain `lastName`.
  - AC-2: controller test for GET `/owners?lastName=X&page=2` uses repo `findByLastNameStartingWith` (can be `@MockBean` verify) and returns `ownersList`.
  - AC-3: controller test for out-of-bounds page redirect URL contains `page=1&lastName=X`.

### COD-161
- AC recap: results page displays active `lastName`; Back to Search pre-fills `lastName`; Clear Filter goes to unfiltered list with no `lastName` param.
- Affected files (verified):
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - Tests: `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - Controller: ensure `showOwnersList()` / list rendering path exposes `lastName` (already needed by COD-158) and expose it to search form for prefill on `/owners/find` (Assumption: `@GetMapping("/owners/find")` exists in OwnerController).
  - View (ownersList): add a small context line rendered only when `lastName` non-empty; add two links/buttons:
    - Back to Search: `/owners/find?lastName=...` (or equivalent route) to prefill.
    - Clear Filter: `/owners` (optionally `?page=1`) without `lastName`.
  - View (findOwners): ensure it can pre-populate from model attribute/binding when query param is present.
  - Tests: validate presence/absence of context line; validate Back/Clear hrefs.
- Test types per AC:
  - AC-1: HTML contains the displayed filter value.
  - AC-2: HTML contains Back to Search link with lastName included.
  - AC-3: HTML contains Clear Filter link without lastName.

### COD-164
- AC recap: search supports `lastName` + `city` (AND); paging retains both; empty results return to search form with existing notFound behavior.
- Affected files (verified):
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - Tests: `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - View (findOwners): add City input field bound to `Owner.address.city` is not available; instead bind to `Owner.city` field (verified `Owner` has `city` property) so we can reuse the Owner backing bean.
  - Controller: extend search handler to read/normalize optional `city` from bound Owner; apply AND filtering when `city` non-empty.
  - Repository: add a new Spring Data method supporting AND + pageable, e.g., `findByLastNameStartingWithAndCity(...)` (exact vs contains is Open Question in FRD).
  - View (ownersList): append `city` to pagination links when present; show criteria summary may include both.
  - Tests: controller tests for combined filtering (verify correct repository method invoked); HTML link tests ensure both params are present; empty result returns `owners/findOwners` with field error.
- Test types per AC:
  - AC-1: MockMvc verifies repo called with both criteria and `ownersList` returned.
  - AC-2: HTML contains paging links with both `lastName` and `city`.
  - AC-3: MockMvc verifies error on form when no results.

## 4) Branch and commit strategy
- Plan branch (this PR): `plan/COD-157-implementation-plan`.
- Feature branch (implementation): `feature/COD-157-owner-search`.
- Commits: one commit per Story, prefixed with key, e.g., `COD-158: preserve lastName in pagination links`.
- One implementation PR for the Epic (not per Story); keep commits separated for review.

## 5) Build, local deployment, and health check
- Build/tests: `./mvnw test` (repo also has Gradle; primary build assumed Maven due to `pom.xml`).
- Run: `./mvnw spring-boot:run` then open `http://localhost:8080/owners/find`.
- Manual checks:
  - Search lastName with multiple pages; verify paging keeps params.
  - Verify Back to Search and Clear Filter.
  - Verify lastName + city search and paging.

## 6) Risks
- Risk: Thymeleaf link building may double-encode/omit params if not done carefully; mitigate with template helper vars and tests (Owner: Tech Lead).
- Risk: City matching requirement ambiguous (exact vs contains/prefix) and affects repository method; mitigate by confirming in story grooming before coding (Owner: Product Owner).

## 7) Definition of Done & approval gates
- DoD: all ACs met; unit/controller tests added/updated; `./mvnw test` green; no regressions in owner search.
- Gates: Plan PR approved → (optional) UI note in Confluence → Code PR opened → Code review → Tests pass in CI → Merge → Verify on deployed env → Docs updated if UI text changes.

## 8) Assumptions & open questions
- Assumption: `OwnerController` has a GET route `/owners/find` used by nav and used for Back to Search; if not, implement link to existing find route.
- Open question (from FRD): city matching exact vs prefix/contains; default to exact (case-insensitive) unless specified.
- Open question: allow city-only search when lastName is blank? (FRD asks; default to current behavior: blank lastName means show all, so city-only likely needs explicit decision).
