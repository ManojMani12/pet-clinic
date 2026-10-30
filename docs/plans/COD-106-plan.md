# COD-106 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: improve owner search usability: preserve search params across pagination, add City filter slice, make page size configurable.
- In scope: COD-88, COD-107, COD-108 (no new entity pages; reuse existing /owners flow).
- Out of scope: sorting, fuzzy search, firstName/telephone filters, cross-entity pagination standards.
- Epic: https://manoj16239.atlassian.net/browse/COD-106
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/30834689/FRD+-+COD-106+-+Owner+search+improvements

## 2) Delivery order (blockers first)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-88 | Preserve owner last-name filter across pagination links | 1 | - |
| 2 | COD-107 | Add City filter to Owner search (first slice) | (not set) | COD-88 |
| 3 | COD-108 | Make Owner search page size configurable (preserved in paging) | (not set) | COD-88 |

## 3) Story plans

### COD-88 — Preserve owner last-name filter across pagination links
- AC recap: paging controls keep lastName in query; out-of-bounds redirect preserves lastName; blank lastName works.
- Affected files (verified):
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI (Thymeleaf): update pagination link construction to include lastName when present.
  - Web/MVC: ensure model exposes the current lastName value used for search (either via bound Owner model or explicit attribute).
  - Tests: add/extend MockMvc tests to verify generated links/requests preserve lastName; verify out-of-bounds redirect keeps lastName.
- Tests per AC:
  - AC-1: MVC test performs /owners?lastName=... and asserts ownersList view contains paging hrefs including lastName.
  - AC-2: MVC test requests an out-of-range page with lastName and asserts 3xx redirect includes lastName.
  - AC-3: MVC test requests /owners?page=2 with blank lastName behavior unchanged (Assumption: dataset has >pageSize owners).

### COD-107 — Add City filter to Owner search (first slice)
- AC recap: optional city filter works alone/with lastName; no-match shows feedback; city preserved across pages.
- Affected files (verified):
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/main/java/org/springframework/samples/petclinic/owner/Owner.java (binding uses existing field city)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI: add City input bound to Owner.city on findOwners form; ensure form submits city as query param.
  - Web/MVC: extend search handling to accept city and include it in redirects + model for list rendering.
  - Data: add repository query method(s) for combined filter. Open question: city match semantics (starts-with vs contains; case sensitivity).
  - UI paging: extend pagination links to preserve both lastName and city.
  - Tests: add MVC tests for (lastName+city) filter, city-only filter, and city preserved in paging links.
- Tests per AC:
  - AC-1: Mock repository expectations / integration slice to validate combined filter path; assert model listOwners contains only matching.
  - AC-2: request with city that yields none and assert findOwners view + field error.
  - AC-3: assert ownersList paging hrefs include both lastName and city.

### COD-108 — Make Owner search page size configurable (preserved in paging)
- AC recap: page size from config; default remains 5; optional request size param (if implemented) bounded and preserved.
- Affected files (verified):
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/resources/templates/owners/ownersList.html (if size param is supported and must be preserved)
  - src/main/resources/application.properties (or yaml) (new/updated property)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java
- Sub-tasks by layer:
  - Config: introduce property (e.g., petclinic.owners.pageSize) with default 5.
  - Web/MVC: replace hard-coded pageSize with configured value; validate config (>=1) and fallback to default.
  - Optional (open): add request parameter size with min/max bounds; preserve size in pagination links.
  - Tests: add tests for default=5, configured size, and invalid config; if size param supported, add bounds/preservation tests.
- Tests per AC:
  - AC-1: SpringBootTest/MVC with overridden property and assert paging uses configured size.
  - AC-2: existing behavior tests ensure default 5.
  - AC-3: if size param implemented, MockMvc verifies bounding and link preservation.

## 4) Branch & commit strategy
- Plan branch (this PR): plan/COD-106-implementation-plan
- Feature branch for implementation (per workflow): feature/COD-106-owner-search
- Commits: one commit per Story, prefixed with Story key (e.g., "COD-88: ...", "COD-107: ...", "COD-108: ...").
- PRs: this plan PR (docs only) + one implementation PR for all 3 stories (or split if review prefers).

## 5) Build, local run, health check
- Build/tests: ./mvnw test (or ./mvnw -DskipTests=false test).
- Run: ./mvnw spring-boot:run
- Verify manually:
  - /owners/find (or Find Owners nav) search with lastName; confirm paging keeps params.
  - Search with city (after COD-107) and verify results + paging.
- Health: check app starts cleanly and /actuator/health if enabled (Assumption: actuator may not be on).

## 6) Risks
- Risk 1 (Tech Lead): ambiguous City matching semantics (starts-with vs contains/case). Mitigation: confirm in design; encode in tests.
- Risk 2 (QA/Dev): pagination link construction becomes error-prone as params grow. Mitigation: centralize query param model attrs and test rendered links.
- Risk 3 (DevOps): misconfigured page size (0/negative) causes runtime issues. Mitigation: validate and fallback; keep invalid-config test coverage.

## 7) Definition of Done & approval gates
- DoD: all ACs pass via automated tests; no regression to existing owner flows; code style checks pass; docs updated if config added.
- Gates: Plan PR approved → (optional) Design notes confirmed in Confluence → Code PR opened → peer review → CI green → QA sign-off → merge.

## 8) Assumptions & open questions
- Assumption: URL for owner search remains /owners with GET params; templates remain Thymeleaf.
- Open Q1: City filter matching semantics and case sensitivity.
- Open Q2: Do we implement request size param? If yes, min/max bounds and UI exposure (hidden vs selector).
- Open Q3: Should blank lastName + city return all owners in that city (per FRD BR-1/BR-2), or require lastName? (FRD suggests allow blank lastName).
