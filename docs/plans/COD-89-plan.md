# COD-89 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Epic: COD-89 — Owner search improvements
- Scope: preserve owner search filters across pagination; add optional city filter; make results page size configurable.
- Out of scope: new filters beyond city; per-user page-size UI.
- Confluence (Design/ARCH): https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/29556737
- FRD: Not found in Confluence (Assumption: requirements captured in Jira stories).

## 2) Delivery order (blockers first)
| Order | Story | Title | Pts | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 1 | — |
| 2 | COD-48 | Add city filter to Owner search (first slice) | 3 | COD-80 |
| 3 | COD-49 | Make Owner search results page size configurable | 2 | COD-80 |

## 3) Story plans

### COD-80
- AC recap: paging links retain active lastName; unfiltered paging does not add lastName; whitespace-normalized lastName persists.
- Affected files: 
  - src/main/resources/templates/owners/ownersList.html
  - (likely) src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI: update all page links (first/prev/next/last/page numbers) to include lastName only when present.
  - Controller: ensure model contains normalized lastName for the view (Assumption: may add model attribute like searchLastName).
  - Tests: add MockMvc assertions that paging URLs include lastName when filtered and omit when not.
- Test types per AC:
  - AC-1: MVC test verifying links include lastName and controller returns filtered list.
  - AC-2: MVC test for no lastName query param in links when unfiltered.
  - AC-3: MVC test ensuring whitespace input results in trimmed value used for subsequent paging.

### COD-48
- AC recap: add optional city filter; notFound behavior preserved; paging preserves lastName+city.
- Affected files:
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI: add City input to findOwners form; ensure it submits as request param.
  - Controller: accept/trim city; include in search + in paging link model attributes.
  - Repository: add new pageable search method supporting lastName prefix + optional city (exact or normalized match—Open question).
  - Tests: add/extend controller tests for city filtering, empty results, and paging param preservation.
- Test types per AC:
  - AC-1: MVC test for combined filters applied.
  - AC-2: MVC test expecting findOwners view with field error when no results.
  - AC-3: MVC test verifying paging links include both lastName and city.

### COD-49
- AC recap: default page size 5; configurable to 10; invalid config falls back to 5.
- Affected files:
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/resources/application.properties (or yaml) (Assumption: exists in repo)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeTests.java
- Sub-tasks by layer:
  - Config: introduce property (e.g., petclinic.owners.pageSize) with default 5.
  - Controller: replace hard-coded pageSize with injected/configured value; validate positive integer.
  - Tests: update/extend page-size tests to cover default, configured, and invalid values.
- Test types per AC:
  - AC-1: Spring Boot test with default properties asserts size==5.
  - AC-2: Spring Boot test overriding property asserts size==10.
  - AC-3: Spring Boot test with invalid property asserts fallback behavior.

## 4) Branch & commit strategy
- Plan branch: plan/COD-89-implementation-plan (this PR only contains the plan).
- Implementation branch (later, by dev team): feature/COD-89-owner-search.
- Commit plan for implementation: one commit per Story, message prefix with Story key (e.g., "COD-80: preserve lastName in paging links"); single code PR targeting main.

## 5) Build / run / health check
- Build: `./mvnw test` (or `mvn test`) and ensure green.
- Run locally: `./mvnw spring-boot:run` then open `http://localhost:8080/`.
- Smoke: navigate to Find Owners, search by last name, confirm paging retains filters; repeat with city filter; verify page size behavior when property set.

## 6) Risks
- Risk: search query semantics for city matching (case sensitivity, contains vs equals) may differ across DBs. Owner: Tech Lead.
- Risk: template link changes could break existing tests or navigation patterns. Owner: Developer.
- Risk: adding new repository query method may require refactoring existing controller flow (0/1/many results). Owner: Developer.

## 7) Definition of Done / approval gates
- Plan PR approved by Tech Lead.
- Design/ARCH page reviewed/updated if needed (Confluence link above).
- Code PR: all ACs met, unit/MVC tests added/updated, `./mvnw test` green.
- Review: at least 1 peer review + Tech Lead sign-off.
- Docs: update any user-facing notes (README not required unless behavior changes are substantial).

## 8) Assumptions & open questions
- Assumption: Owners module uses Thymeleaf templates listed above (verified) and OwnerController handles pagination (verified).
- Open question: City match should be exact vs case-insensitive vs prefix/contains; decide and document in repository method name/query.
- Open question: Where to define config property (application.properties vs dedicated @ConfigurationProperties class) for page size.
