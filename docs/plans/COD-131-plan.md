# COD-131 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Scope: preserve active filters in Owner search pagination, add City filter, make page size configurable.
- Out of scope: firstName/telephone filters, sorting, fuzzy search, UI page-size selector.
- Jira Epic: COD-131 (Owner search improvements)
- Stories: COD-80, COD-98, COD-48
- FRD (Confluence): https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/32374785/FRD+-+COD-131+-+Owner+search+improvements

## 2) Delivery order (by blockers)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 2 | — |
| 2 | COD-98 | Make Owner search results page size configurable | 2 | COD-80 |
| 3 | COD-48 | Add city filter to Owner search (first slice) | 3 | COD-80 |

## 3) Story plans

### COD-80 — Preserve last name filter across Owner results pagination
- AC recap: paging links include lastName when provided; unfiltered paging doesn’t introduce lastName; whitespace-trimmed lastName is used.
- Affected files (verified):
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java (or new dedicated test class)
- Sub-tasks by layer:
  - UI (Thymeleaf): update all paging links (first/prev/next/last + page numbers) to append lastName param only when present.
  - Controller: ensure model exposes the normalized lastName (trimmed) for view link generation (if not already accessible).
  - Tests: add MVC test verifying model/view renders links containing lastName=Fra for multi-page scenario; verify absent lastName doesn’t appear.
- Test types per AC:
  - AC-1: MockMvc test stubbing repo Page with totalPages>1; assert response content contains "lastName=Fra" in links.
  - AC-2: MockMvc test calling /owners?page=2 without lastName; assert links do not contain "lastName=".
  - AC-3: MockMvc test calling /owners?lastName=%20Fra%20&page=2; assert links contain trimmed "Fra".

### COD-98 — Make Owner search results page size configurable
- AC recap: default 5 when not configured; honor petclinic.owners.pageSize; invalid/non-positive falls back to 5.
- Affected files (verified):
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeConfigTests.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java
  - src/main/resources/application.properties (Assumption: exists) / src/main/resources/application.yml (Assumption)
- Sub-tasks by layer:
  - Config/Controller: introduce pageSize field injected from property with default 5; validate >0 else use 5.
  - Controller: update pageable construction to use configured pageSize.
  - Tests: update/align page-size tests to match injection mechanism (these tests currently imply a constructor-based config).
- Test types per AC:
  - AC-1: Spring slice test with default properties; verify repository called with Pageable size 5.
  - AC-2: Spring slice test with TestPropertySource("petclinic.owners.pageSize=10"); verify Pageable size 10.
  - AC-3: Spring slice test with non-positive; verify fallback size 5 and no exception.

### COD-48 — Add city filter to Owner search (first slice)
- AC recap: filter by lastName+city; no results shows existing notFound behavior; paging preserves both lastName and city.
- Affected files (verified):
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java (plus new tests)
- Sub-tasks by layer:
  - UI: add City input to Find Owners form bound to owner.city (Owner has city field).
  - Repository: add a query method supporting optional city with paging (exact/contains semantics per design; currently open question).
  - Controller: normalize city (trim; blank->null) and call appropriate repository method; include city in model for link generation.
  - UI paging: extend COD-80 approach to include both lastName and city when present.
  - Tests: add MVC tests for combined filters and for paging link preservation of both params.
- Test types per AC:
  - AC-1: MockMvc test with lastName=Davis&city=Madison; verify repo called with both params and view shows list.
  - AC-2: MockMvc test where repo returns empty; assert findOwners view and field error notFound.
  - AC-3: MockMvc test multi-page scenario; assert links contain lastName and city.

## 4) Branch & commit strategy
- Plan branch (this PR): plan/COD-131-implementation-plan (one commit).
- Implementation branch (for code work): feature/COD-131-owner-search.
- Commit per Story on implementation branch, message prefix: "COD-80:", "COD-98:", "COD-48:"; single code PR targeting main.

## 5) Build, local run, health checks
- Build/tests: ./mvnw test (repo also contains Gradle; prefer Maven per baseline).
- Run: ./mvnw spring-boot:run; verify Owner search at /owners/find.
- Health check: open /actuator/health (Assumption: actuator enabled) and validate 200; manually verify pagination links preserve params.

## 6) Risks
- Risk: City match semantics (exact vs contains, case sensitivity) unclear; could cause rework. Owner: Product + Tech Lead.
- Risk: Page size tests currently expect constructor-based config; refactor may require test rework and could break other tests. Owner: Tech Lead.
- Risk: Thymeleaf link-building with conditional params can get verbose and error-prone. Owner: Developer.

## 7) Definition of Done / approval gates
- Plan PR approved by Tech Lead.
- Design confirmed in Confluence (city matching semantics, case-sensitivity, property name).
- Code PR: all tests pass, new tests added for new ACs, no regression in Owner search.
- QA: manual validation of /owners/find → results paging; verify both filtered/unfiltered cases.
- Docs: update README/config docs if property added (optional).

## 8) Assumptions & open questions
- Assumption: src/main/resources/application.properties exists and is the right place for default property.
- Open question: City matching semantics and case-insensitivity (FRD flagged as open).
- Open question: Should paging links preserve empty params (e.g., city="") or omit them entirely? (Recommendation: omit if blank).
