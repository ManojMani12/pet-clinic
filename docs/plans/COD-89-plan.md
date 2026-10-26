# COD-89 Implementation Plan — Owner search improvements

## 1) Summary, scope, links
- Epic: COD-89 (Owner search improvements)
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/29065220
- Scope: Owner search paging preserves filters; add City filter; make page size configurable.
- Out of scope: firstName/telephone search; fuzzy matching; sorting enhancements.

## 2) Delivery order
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 2 | — |
| 2 | COD-49 | Make Owner search results page size configurable | 2 | COD-80 |
| 3 | COD-48 | Add city filter to Owner search (first slice) | 3 | COD-80 |

## 3) Story plans

### COD-80
- AC recap: paging links keep lastName; unfiltered paging doesn’t add lastName; whitespace-normalized lastName persists.
- Affected files: 
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java (or new focused MVC test)
- Sub-tasks (by layer):
  - UI (Thymeleaf): update page/prev/next/first/last links to include lastName when present in model.
  - Web (Controller): ensure addPaginationModel (or equivalent) includes normalized lastName in model for the view.
  - Tests: MockMvc test(s) asserting rendered links (href contains lastName=...) and that controller calls repository with expected criteria.
- Tests per AC:
  - AC1: MVC test that /owners?lastName=Franklin renders ownersList with pagination links containing lastName=Franklin.
  - AC2: MVC test that /owners?page=2 (no lastName) renders links without lastName.
  - AC3: MVC test that /owners?lastName=%20%20Franklin%20%20 renders links with lastName=Franklin (normalized).

### COD-49
- AC recap: default page size=5; config page size=10; invalid/non-positive falls back to 5.
- Affected files:
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/resources/application.properties (or application.yml) (optional: document property)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeTests.java
- Sub-tasks (by layer):
  - Config: introduce property petclinic.owners.pageSize with default 5 (use @Value or @ConfigurationProperties).
  - Web (Controller): replace hard-coded pageSize with configured value; validate >0 else default.
  - Tests: ensure existing page-size test passes; add test for invalid value fallback if missing.
- Tests per AC:
  - AC1: existing tests validating default behavior (or add one if absent).
  - AC2: OwnerControllerPageSizeTests with TestPropertySource=10 (already present).
  - AC3: new test with TestPropertySource=0 (or -1) asserting PageRequest size=5.

### COD-48
- AC recap: lastName+city filters; no-results stays on findOwners with notFound; paging preserves lastName+city.
- Affected files:
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html (paging links must preserve city too)
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks (by layer):
  - UI: add City input bound to Owner.city on Find Owners form.
  - Web (Controller): normalize city (trim/strip); pass both criteria into repository; include normalized criteria in model for paging.
  - Data (Repository): add new search method supporting lastName starts-with + optional city filter with Pageable (likely @Query to conditionally apply city).
  - Tests: add MockMvc tests covering criteria handling, notFound path, and pagination URL preservation for both params.
- Tests per AC:
  - AC1: MVC test verifying repository called with expected lastName/city criteria and ownersList returned.
  - AC2: MVC test with repo returning empty page to assert findOwners + field error.
  - AC3: MVC test asserting pagination link href includes both lastName and city.

## 4) Branch and commit strategy
- Plan branch: plan/COD-89-implementation-plan (this PR only).
- Implementation branch (for code changes): feature/COD-89-owner-search
- Commit approach (implementation PR): one commit per Story, prefix commit message with Story key (e.g., "COD-80: preserve filters in paging links").
- PR approach (implementation): single PR for the Epic, merged via required reviews.

## 5) Build, local deployment, health check
- Build: `./mvnw clean test`
- Run: `./mvnw spring-boot:run` (Java 17)
- Manual check:
  - Open `/owners/find` and search; verify pagination retains filters.
  - Verify city filter affects results.
  - Set `petclinic.owners.pageSize` and confirm owners per page changes.

## 6) Risks
- Risk: Thymeleaf link generation may inadvertently double-encode or omit params (Owner: Frontend dev).
- Risk: Repository query for optional city may be non-performant or incorrect across H2/JPA (Owner: Backend dev).
- Risk: Existing tests assert specific URLs/attributes and may become brittle after adding params (Owner: QA/Dev).

## 7) Definition of Done & approval gates
- Plan PR approved by Tech Lead.
- Design/FRD confirmed in Confluence (CodAI) and linked in PR.
- Implementation PR: code reviewed, CI green, unit/MVC tests updated/added.
- Acceptance: manual verification of paging + city filter + configured page size.
- Docs: property documented (README or application.properties comment) as needed.

## 8) Assumptions and open questions
- Assumption: City match is exact (case sensitivity TBD); if case-insensitive is required, confirm before implementing query.
- Question: Should paging links include parameters only when present (recommended) vs always include empty values?
- Question: Should city filter be exact match or starts-with/contains? (FRD says “matches” — clarify behavior if needed).
