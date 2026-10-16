# COD-6 – Implementation Plan: Owner search improvements

## 1) Summary / scope / links
- **Goal:** Improve Owner search in Spring PetClinic by (1) preserving filters across pagination, (2) adding **city** as a filter (first slice), and (3) showing active criteria with a “Refine search” navigation link.
- **In scope:** Owners search form (`findOwners`), owners results list (`ownersList`), controller/repository query support, and automated tests.
- **Out of scope:** Adding new entities, major UI redesign, non-owner search areas.
- **Epic:** https://manoj16239.atlassian.net/browse/COD-6
- **FRD:** https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/24805377/FRD+-+COD-6+-+Owner+search+improvements

## 2) Delivery order (blockers first)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-7 | Preserve owner search filters across pagination | 2 | – |
| 2 | COD-8 | Add city filter to Owner search (first slice) | 3 | COD-7 |
| 3 | COD-9 | Show active Owner search criteria and provide Refine Search link | 2 | COD-7 |

## 3) Story plans

### COD-7
- **AC recap:** When user searches owners (e.g., `lastName` filter) and navigates next/prev pages, the filter remains applied.
- **Affected files (verified):**
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java` (only if pagination currently drops params server-side)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- **Sub-tasks by layer:**
  - UI (Thymeleaf): ensure pagination links include current query params (at least `lastName`; later `city`).
  - Web (Controller): confirm request mapping retains/searches using incoming query params on paged requests; pass criteria back to model.
  - Tests: extend controller MVC tests to assert page links contain `lastName` and that requesting page 2 with same params returns filtered results.
- **Tests per AC:**
  - `@WebMvcTest` / MockMvc: search with `lastName`, assert model + generated pagination URLs include the param; request a second page with param and assert same filter behavior.

### COD-8
- **AC recap:** Add `city` filter to Owner search (GET), and results should be filtered by **lastName AND city** when both provided.
- **Affected files (verified):**
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- **Sub-tasks by layer:**
  - UI: add City input to find form; submit via GET query param (alongside `lastName`).
  - Web: update controller to bind/accept `city` parameter, keep it in the model for redisplay and paging.
  - Data: add repository query method supporting AND semantics (e.g., lastName like + city like/exact per existing conventions).
  - Tests: add MVC tests for combinations: only lastName, only city, both; and pagination with both.
- **Tests per AC:**
  - `@DataJpaTest` (if repo currently unit-tested) or controller tests with mocked repository: verify correct repository method invoked for both criteria.

### COD-9
- **AC recap:** On owners results page, show active criteria summary (e.g., “Last name: X, City: Y”) and provide a “Refine search” link back to find form with fields prefilled.
- **Affected files (verified):**
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- **Sub-tasks by layer:**
  - UI: add criteria summary section rendered conditionally when filters present.
  - UI: add “Refine search” link to `/owners/find` carrying current criteria as query params.
  - Web: ensure controller populates model attributes needed for summary/prefill (likely echo back `lastName`, `city`).
  - Tests: assert summary text presence and refine link URL contains active params.
- **Tests per AC:**
  - MockMvc HTML assertions: criteria summary exists when params are present; refine link includes query params; find page shows prefilled values (as rendered inputs).

## 4) Branch & commit strategy
- **Plan branch (this PR only):** `plan/COD-6-implementation-plan`
- **Implementation branch (later, per plan):** `feature/COD-6-owner-search`
- **Commits (later):** one commit per Story, prefixed with story key:
  - `COD-7: preserve filters across owners pagination`
  - `COD-8: add city filter to owner search`
  - `COD-9: show active criteria + refine link`
- **PRs:** 1 implementation PR for the Epic (unless Tech Lead requests splitting).

## 5) Build / local run / health check
- Build & tests: `./mvnw test` (or `mvn test`) and `./mvnw -DskipTests=false verify`.
- Run app: `./mvnw spring-boot:run` then open `http://localhost:8080/`.
- Manual checks:
  - Owners → Find owners; search by last name; verify pagination retains params.
  - Search by city; search by both; verify results.
  - Verify criteria summary and refine link prefill.

## 6) Risks
- **Risk:** Pagination link generation is duplicated/hand-rolled in template and easy to miss when adding new params (Owner: Tech Lead).
- **Risk:** Repository query semantics (contains vs exact, case sensitivity) may diverge from FRD expectations (Owner: Backend Dev).
- **Risk:** HTML assertions in tests brittle if template structure changes (Owner: QA/Dev).

## 7) Definition of Done & approval gates
- Plan PR approved by Tech Lead.
- Design/FRD aligned (Confluence FRD link present in Jira).
- Implementation PR passes CI: unit/integration tests green, no regressions.
- Acceptance criteria demoed: pagination preserves filters; city filter works; criteria summary + refine link work.
- Docs updated if user-facing behavior changes (README or in-app help if any).

## 8) Assumptions & open questions
- **Assumption:** Owner search uses a paged list view (`ownersList.html`) with query-param-based navigation.
- **Assumption:** Search endpoint is under `/owners` and find form under `/owners/find` (verify in controller during implementation).
- **Open Q:** Should `city` filtering be exact match or “contains/starts-with” (case-insensitive)? FRD should specify; if not, decide and document.
- **Open Q:** When both filters are empty, should behavior remain “list all” or show validation/message? (Do not change existing behavior unless FRD says so.)
