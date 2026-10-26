# COD-46 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Epic: COD-46 — Owner search improvements
- Goal: preserve owner search filters across paging, add City filter, make page size configurable.
- In-scope Stories: COD-71, COD-72, COD-73.
- Out of scope: additional filters beyond City (e.g., first name/telephone), fuzzy/full-text search.
- FRD (Confluence): https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/28508162/FRD+-+COD-46+-+Owner+search+improvements+v2
- Repo: https://github.com/ManojMani12/pet-clinic

## 2) Delivery order (by blockers)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-71 | Preserve lastName filter in owner results paging | 2 | — |
| 2 | COD-72 | Add city filter to owner search (first slice) | 3 | COD-71 |
| 3 | COD-73 | Make owner search page size configurable | 2 | COD-71 |

## 3) Story plans

### COD-71
- AC recap: paging controls keep lastName in URL/results; out-of-range redirect to page=1 retains lastName; blank lastName stays unfiltered.
- Affected files:
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks (by layer):
  - UI: update all pagination links (page numbers, first/prev/next/last) to include lastName when present.
  - Web/MVC: ensure model exposes the active lastName value to the ownersList view (may already be available via request param; verify and add explicit model attribute if needed).
  - (No DB changes expected.)
- Tests per AC:
  - MVC test: GET /owners?lastName=…&page=2 returns filtered page; verify model/response indicates search criteria preserved.
  - MVC test: out-of-range page redirects to page=1 and includes lastName (assert redirectedUrl).
  - MVC test: when lastName blank/omitted, paging works without lastName param.

### COD-72
- AC recap: city-only search filters owners by city; no matches returns findOwners with validation message; paging retains lastName and city together (AND).
- Affected files:
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks (by layer):
  - UI: add City input to findOwners form; ensure results paging links preserve both lastName and city.
  - Web/MVC: extend processFindForm to bind/trim city and pass it through paging/search flow; retain 0/1/many behavior.
  - Data: add repository method(s) to support city filtering and combined lastName+city filtering (AND). (Exact/contains semantics unresolved — see Open Questions.)
- Tests per AC:
  - MVC test: city-only search returns ownersList with owners in that city.
  - MVC test: lastName+city returns narrowed results (AND).
  - MVC test: no matches returns owners/findOwners with field error/message.
  - MVC test: paging request with both params stays filtered; verify redirect URLs (if any) keep both.

### COD-73
- AC recap: page size reads from config (default 5); when configured (e.g., 10) results show that many per page; filters still preserved.
- Affected files:
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/resources/application.properties (or equivalent)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks (by layer):
  - Config: introduce application property (e.g., petclinic.owners.pageSize) with default 5.
  - Web/MVC: replace hard-coded page size in findPaginatedForOwnersLastName with configured value.
  - (Optional) Add a small @ConfigurationProperties holder if preferred; keep change minimal.
- Tests per AC:
  - Spring test: default page size remains 5 (no property set).
  - Spring test: override property to 10 and assert paging uses 10.
  - Regression: filtered paging still includes lastName/city and uses configured size.

## 4) Branch & commit strategy
- Plan branch: plan/COD-46-implementation-plan (this PR).
- Implementation branch (for dev): feature/COD-46-owner-search.
- Commits: one commit per Story, prefixed with Story key (e.g., "COD-71: preserve lastName in paging links").
- PRs: single code PR for the Epic unless reviewers request split; keep commits story-scoped.

## 5) Build / run / health check
- Build & test: ./mvnw test (or mvn test) on Java 17.
- Run locally: ./mvnw spring-boot:run
- Smoke checks:
  - /owners/find search with lastName and city; page through results and confirm URL params preserved.
  - Change page size config and confirm result count per page.

## 6) Risks
- Risk: City matching semantics (exact vs contains vs starts-with) unclear; may cause rework. Owner: Product Owner.
- Risk: Paging link updates may miss one control or break Thymeleaf expressions. Owner: Frontend/Thymeleaf dev.
- Risk: Tests may rely on existing dataset ordering; paging assertions could be flaky. Owner: QA/Dev.

## 7) Definition of Done & approval gates
- Plan PR approved by Tech Lead.
- Confluence FRD linked and up to date.
- Code PR meets ACs, includes/updates MVC tests, passes CI.
- Review: at least 1 approving review; no critical findings.
- Docs: update any user-facing notes (if applicable) and ensure configuration property documented.

## 8) Assumptions & open questions
- Assumption: ownersList.html is the only place rendering pagination controls for owner search.
- Open Q1: City filter matching: exact match vs starts-with vs contains? (FRD/Jira does not specify.)
- Open Q2: When out-of-range page redirects to page=1, must it preserve *all* active filters (lastName + city) consistently? (Recommend yes.)
- Open Q3: Property name for page size — confirm preferred naming convention (proposal: petclinic.owners.pageSize).
