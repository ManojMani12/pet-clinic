# COD-87 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: preserve search criteria in Owner results pagination, add optional City filter, make page size configurable.
- In scope: `GET /owners` flow + templates `owners/findOwners.html`, `owners/ownersList.html`, controller/repo, MVC tests.
- Out of scope: new filters beyond City, fuzzy matching, sorting changes, runtime UI page-size selection.
- Epic: COD-87 (Jira)
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/29130753/FRD+-+COD-87+-+Owner+search+improvements

## 2) Delivery order (blockers first)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 2 | - |
| 2 | COD-49 | Make Owner search results page size configurable | 2 | COD-80 |
| 3 | COD-48 | Add city filter to Owner search (first slice) | 3 | COD-80 |

## 3) Story plans

### COD-80
- AC recap: pagination links must keep `lastName`; trimmed value persists across pages; empty lastName behaves as today.
- Affected files:
  - `src/main/resources/templates/owners/ownersList.html`
  - (possibly) `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java` (model attribute for criteria)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java` (add coverage) 
- Sub-tasks by layer:
  - UI: update first/prev/next/last + page-number URLs to append `&lastName=...` when non-empty.
  - Web/controller: ensure view has access to the active (trimmed) `lastName` (add to model if not already).
  - Tests: add MockMvc test verifying generated pagination links include `lastName` and that controller receives it when paging.
- Test types per AC:
  - AC-1: MVC test that asserts `owners/ownersList` contains links with `lastName=Frank` and page navigation works.
  - AC-2: MVC test for no-results path unchanged (still returns `owners/findOwners`).
  - AC-3: MVC test ensuring whitespace is trimmed and paging preserves trimmed value.

### COD-49
- AC recap: page size comes from `petclinic.owners.pageSize` with default 5; configured value affects `PageRequest` and total pages; invalid (<=0) handling TBD.
- Affected files:
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeTests.java`
  - (optional) `src/main/resources/application.properties` (document property) 
- Sub-tasks by layer:
  - Config/web: inject configurable page size into `OwnerController` (e.g., via `@Value` or config properties) and use it in paging.
  - Validation: decide behavior for invalid values (<=0): fallback to 5 (Assumption) or fail-fast (open question).
  - Tests: ensure `OwnerControllerPageSizeTests` passes and add negative/zero page size test if behavior is defined.
- Test types per AC:
  - AC-1: existing `OwnerControllerPageSizeTests` verifying `PageRequest` size is 10.
  - AC-2: MVC test verifying default remains 5 when property absent (can be a new test).
  - AC-3: config validation test once decision is confirmed (documented as open question until confirmed).

### COD-48
- AC recap: add optional City filter; when provided, result must satisfy lastName prefix AND city match; trim city; preserve city across paging.
- Affected files:
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/Owner.java` (only if city field access/binding requires changes; likely already exists)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: add City input bound to the Owner model on findOwners form.
  - Repo: extend repository to support combined filter with paging (derived query or explicit query) (Assumption: city stored on Owner).
  - Controller: read/trim city; branch logic: if city empty -> existing behavior; else combined filter; add criteria to model for ownersList.
  - UI paging: extend COD-80 link logic to also include `city` when present.
  - Tests: MockMvc tests for combined filter, no-results, trim + paging persistence.
- Test types per AC:
  - AC-1: MVC test verifying repository called with combined criteria and results view renders.
  - AC-2: MVC test verifying no-results returns `owners/findOwners` with existing validation message.
  - AC-3: MVC test ensuring `city` trimmed and appears in pagination URLs.

## 4) Branch and commit strategy
- Plan branch (this PR): `plan/COD-87-implementation-plan` (single commit).
- Implementation branch (for dev work): `feature/COD-87-owner-search`.
- One commit per Story during implementation, prefixed with story key (e.g., `COD-80: preserve lastName in pagination`).
- Single implementation PR for the Epic (contains all story commits), linked back to the Epic and stories.

## 5) Build / local run / health check
- Build: `./mvnw -q test` (or `./mvnw test`) 
- Run: `./mvnw spring-boot:run`
- Manual check:
  - Navigate to Find Owners; search by last name; confirm paging keeps criteria.
  - Set `petclinic.owners.pageSize=10`; confirm 10 items/page.
  - Search with last name + city; confirm results and paging preserve both.

## 6) Risks
- Risk: Repository query for city+lastName may have case-sensitivity differences across DBs (Owner: QA to verify; Dev to codify via tests).
- Risk: Criteria propagation to UI can regress existing paging links (Owner: Dev; mitigation: MVC link assertions).
- Risk: Unclear handling for invalid pageSize (<=0) may cause startup/runtime issues (Owner: Tech Lead to decide; Dev implements accordingly).

## 7) Definition of Done & approval gates
- Plan PR approved by Tech Lead.
- Confluence FRD reviewed/approved (already created for COD-87).
- Code PR: all stories completed, tests passing, no regressions in owner flows.
- Review: at least 1 reviewer + Tech Lead sign-off.
- Test: unit/MVC tests green; manual smoke checks documented in PR.
- Docs: property `petclinic.owners.pageSize` documented (README or application.properties comment).

## 8) Assumptions & open questions
- Assumption: For `petclinic.owners.pageSize <= 0`, fallback to default 5 (needs Tech Lead confirmation).
- Assumption: Owner has a `city` field already persisted and bound in `Owner` entity.
- Open: Should paging URLs include `lastName`/`city` only when non-empty, or always include blank values?
- Open: Exact city matching rules (case sensitivity) — codify expected behavior in tests.
