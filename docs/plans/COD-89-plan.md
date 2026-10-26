# COD-89 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Epic: COD-89 — Owner search improvements
- Scope: preserve owner search filters during pagination; add City filter (AND with lastName prefix); make owner page size configurable w/ default fallback.
- Out of scope: firstName/telephone filters; sort-order changes beyond what is required for stable paging (document if needed).
- Jira: https://manoj16239.atlassian.net/browse/COD-89
- Stories: COD-80, COD-48, COD-49
- FRD (Confluence): Not found in CodAI space via API (Assumption: will be added later).

## 2) Delivery order
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 2 | — |
| 2 | COD-48 | Add city filter to Owner search (first slice) | 3 | COD-80 |
| 3 | COD-49 | Make Owner search results page size configurable | 2 | COD-80 |

## 3) Story plans

### COD-80
- AC recap: (1) paging controls keep lastName in URL and results stay filtered; (2) no change for not-found; (3) trimmed lastName preserved across pages.
- Affected files:
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java (only if model needs explicit lastName)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI (Thymeleaf): update First/Prev/Next/Last and numeric page links to include lastName query param when present.
  - Web/MVC: ensure model provides trimmed lastName to template (likely via bound "owner" attribute; verify).
  - Tests: add MVC test that renders ownersList and asserts pagination links include lastName; keep existing redirect tests.
- Test types per AC:
  - AC-1: MockMvc GET /owners?lastName=Frank&page=2 -> view owners/ownersList; assert model has lastName and HTML contains "lastName=Frank" in paging URLs.
  - AC-2: existing test verifying findOwners view and field error remains.
  - AC-3: MockMvc GET /owners?lastName=%20%20Fr%20%20&page=2 -> assert links contain lastName=Fr.

### COD-48
- AC recap: (1) lastName prefix + city match; (2) no owners found -> return findOwners w/ error; (3) city trimmed and preserved in paging URLs.
- Affected files:
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI (form): add input bound to Owner.city on findOwners.html.
  - Web/MVC: in processFindForm, normalize city similarly to lastName (null/blank -> "" or null; strip whitespace).
  - Data: add repository method supporting lastName prefix AND city match with Pageable (choose exact semantics; likely equality).
  - UI (results): extend COD-80 link pattern to also include city param when provided.
  - Tests: add controller tests covering filtering with both params; ensure paging preserves both.
- Test types per AC:
  - AC-1: Mock repository to return Page with expected owners; MockMvc GET /owners?lastName=Fr&city=Madison -> ownersList.
  - AC-2: Mock repository to return empty list -> findOwners w/ lastName notFound error (confirm whether same error key applies when city is set).
  - AC-3: MockMvc GET /owners?lastName=Fr&city=%20Madison%20&page=2 -> verify links contain city=Madison and lastName=Fr.

### COD-49
- AC recap: (1) property petclinic.owners.pageSize honored; (2) default 5 if unset; (3) invalid <=0 falls back to 5 without errors.
- Affected files:
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/resources/application.properties (or application.yml if used)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeTests.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java (if needed)
- Sub-tasks by layer:
  - Config: introduce property petclinic.owners.pageSize with default 5.
  - Web/MVC: inject configured value into OwnerController and use it instead of hard-coded 5; guard for <=0.
  - Tests: extend page-size tests for set/unset/invalid property values.
- Test types per AC:
  - AC-1: Spring test with property override; assert controller uses Pageable size 10 (via repository interaction verification).
  - AC-2: default context -> size 5.
  - AC-3: property 0/-1 -> size 5 and request succeeds.

## 4) Branch + commit strategy
- Plan branch: plan/COD-89-implementation-plan
- Implementation branch (for dev team later): feature/COD-89-owner-search
- Commits: one commit per Story, prefixed with key (e.g., "COD-80: preserve filters in pagination").
- One code PR for the Epic (separate from this plan PR).

## 5) Build / run / health check
- Build: ./mvnw test (repo also includes Gradle; use Maven as primary per project standard).
- Run locally: ./mvnw spring-boot:run
- Verify:
  - Open http://localhost:8080/owners/find; search by last name and page through results; confirm query params persist.
  - With city filter: search lastName + city; confirm AND filtering and paging persistence.
  - Change page size via application.properties or -Dpetclinic.owners.pageSize=10; confirm results per page.

## 6) Risks
- Risk (Tech Lead): City match semantics (exact/case) may surprise users; document via tests and keep consistent with H2 collation.
- Risk (QA): Pagination link updates are template-heavy; easy to miss one control (First/Prev/Next/Last). Add HTML assertions in tests.
- Risk (Dev): Binding city onto Owner model may affect other flows if reused; confirm it’s only used for search form binding.

## 7) Definition of Done + approval gates
- Plan PR approved by Tech Lead.
- (Optional) FRD/Design page in Confluence linked from Epic.
- Code PR: all Stories implemented with tests passing (./mvnw test), reviewers approved.
- Regression: manual verification of owner find + paging + city filter.
- Docs: update any relevant README/config docs if property added.

## 8) Assumptions / open questions
- Assumption: ownersList pagination is rendered for filtered and unfiltered searches alike; we will preserve only non-empty params.
- Open question: City matching should be exact or case-insensitive? (Current AC says “matches”; implement + test chosen behavior.)
- Open question: For not-found with city set, error should remain on lastName field (current behavior) vs a global message.
