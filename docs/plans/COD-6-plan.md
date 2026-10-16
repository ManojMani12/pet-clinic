# COD-6: Implementation plan – Owner search improvements

## 1) Summary, scope, links (Epic, FRD)
- **Epic**: COD-6 – Owner search improvements: https://manoj16239.atlassian.net/browse/COD-6
- **FRD**: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/24805377/FRD+-+COD-6+-+Owner+search+improvements
- **Stories**: COD-7, COD-8, COD-9
- **Scope**: preserve search query params across pagination; add City filter; show active criteria + “Refine search” link.
- **Out of scope**: page size changes, additional filters beyond city, major UX redesign.

## 2) Delivery order (dependency-driven)

| Order | Story key | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-7 | Preserve owner search filters across pagination | TBD (**Assumption**) | — |
| 2 | COD-8 | Add city filter to Owner search (first slice) | TBD (**Assumption**) | COD-7 |
| 3 | COD-9 | Show active Owner search criteria and provide Refine search link | TBD (**Assumption**) | COD-7 |

## 3) Story detail

## COD-7: Preserve owner search filters across pagination
- **AC recap**: Pagination links include `lastName`; results remain filtered when paging; notFound behavior unchanged; out-of-range page redirects to page 1 and preserves `lastName`.
- **Affected files (real code)**:
  - Controller: `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - Template: `src/main/resources/templates/owners/ownersList.html`
  - Tests: `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- **Sub-tasks (by layer)**:
  - UI (Thymeleaf): Update page/first/prev/next/last links to include active filter query params (at least `lastName`).
  - Web/Controller: Ensure model exposes normalized active criteria (e.g., normalized `lastName`) so Thymeleaf can build links without string concatenation.
  - Tests: Add/adjust MockMvc tests to verify generated pagination `href`s preserve `lastName`; confirm redirect-to-page-1 retains `lastName`.
- **Test types per AC**:
  - AC (pagination link preservation): WebMvc (MockMvc) + HTML content assertions for `href`.
  - AC (out-of-range redirect): WebMvc redirect assertion.

## COD-8: Add city filter to Owner search (first slice)
- **AC recap**: Search by `lastName` prefix AND `city`; if city blank, behavior matches current; no-match still returns to find form with notFound on lastName; pagination preserves filters.
- **Affected files (real code)**:
  - Controller: `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - Repository: `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - Templates: 
    - `src/main/resources/templates/owners/findOwners.html`
    - `src/main/resources/templates/owners/ownersList.html`
  - Tests: `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- **Sub-tasks (by layer)**:
  - UI (Thymeleaf): Add a City input to the Find Owners form and submit via GET (`/owners?...`) so filters appear as query params.
  - Repository: Add a query method to combine lastName + city (**Assumption**: city match should be case-insensitive exact match; confirm with PO).
  - Web/Controller: Normalize/trim `city`; if city blank then use existing lastName-only repository method; if city present then use combined query; ensure pagination redirect-to-page-1 also preserves `city`.
  - Tests: MockMvc tests verifying controller calls the correct repository method when city is present; verify notFound behavior unchanged; verify blank-city falls back to current behavior.
- **Test types per AC**:
  - WebMvc (MockMvc): controller behavior and view/redirect assertions; Mockito verification of repository method selection.

## COD-9: Show active Owner search criteria and provide Refine search link
- **AC recap**: Owners list shows active criteria summary (no “null” text); “Refine search” navigates to `/owners/find` and pre-fills form with current criteria; criteria summary matches normalized values.
- **Affected files (real code)**:
  - Controller: `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - Templates:
    - `src/main/resources/templates/owners/ownersList.html`
    - `src/main/resources/templates/owners/findOwners.html`
  - Tests: `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- **Sub-tasks (by layer)**:
  - Web/Controller: Standardize model attributes for active criteria (e.g., `searchLastName`, `searchCity`) to reuse for pagination links and summary display.
  - UI (Thymeleaf): Add a criteria summary block on `ownersList`; add a “Refine search” link to `/owners/find` including current criteria as query params; ensure optional params don’t render as “null”.
  - UI (Thymeleaf): Ensure the Find Owners form is pre-populated from the GET-bound model/query params.
  - Tests: MockMvc assertions that criteria text appears/doesn’t appear as expected and that the refine link includes correct query params.
- **Test types per AC**:
  - WebMvc (MockMvc): view HTML content assertions for summary and refine link; GET `/owners/find?...` form prefill assertions.

## 4) Branch and commit strategy
- **Plan branch**: `plan/COD-6-implementation-plan` (this plan PR only).
- **Implementation branch (for dev)**: `feature/COD-6-owner-search` (**Assumption** naming; dev-owned).
- **Commits**: one commit per Story, prefixed with story key (`COD-7: ...`, `COD-8: ...`, `COD-9: ...`).
- **PRs**: single code PR for the Epic; keep stories delivered in the order above.

## 5) Build, local deployment and health check
- Build/tests: `./mvnw clean test`
- Run locally: `./mvnw spring-boot:run`
- Smoke checks:
  - `/owners/find` search by last name, paginate results (query params preserved).
  - Add city filter and paginate (both params preserved).
  - Verify criteria summary and “Refine search” link; ensure refine pre-fills the form.

## 6) Risks (at least 2, with owner role)
- Risk: Thymeleaf query-param link building with optional params can produce malformed URLs or drop params. **Owner**: Tech Lead.
- Risk: City matching semantics (exact vs contains, case sensitivity) unclear and can change repository query design. **Owner**: Product Owner.

## 7) Definition of Done and approval gates
- Plan PR approved by Tech Lead (file: `docs/plans/COD-6-plan.md`).
- Design/spec (FRD) linked from Jira and up to date.
- Code PR: CI green; tests pass; ACs met for COD-7/8/9.
- Review: code review + UI smoke test by QA/Tech Lead.
- Deploy: merged to main via PR, app starts cleanly.
- Docs: update user-facing docs only if flow changes warrant it.

## 8) Assumptions and open questions
- Assumption: Story points not retrieved here; table marked TBD.
- Open question (COD-8): City match should be exact or partial? case-insensitive? (Assumed exact + case-insensitive).
- Open question (COD-9): Refine search should use query params (bookmarkable) vs server-side session/state (recommend query params).