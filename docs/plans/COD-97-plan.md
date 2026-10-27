# COD-97 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: improve Owner search UX by preserving filters during paging, adding City filter (first slice), and making page size configurable.
- In scope: Thymeleaf paging link fixes; add City field to search form; controller+repository updates; config-driven page size; tests.
- Out of scope: new search types beyond City; fuzzy search rules beyond current "startsWith" unless already present.
- Epic: COD-97 (Jira). Stories: COD-80, COD-48, COD-49.
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/30048257/FRD+-+COD-97+-+Owner+search+improvements (from REQUIREMENTS handoff).

## 2) Delivery order (by blockers)
| Order | Story | Title | Points | Blocked by |
|------:|------|-------|-------:|-----------|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 2 | — |
| 2 | COD-48 | Add city filter to Owner search (first slice) | 3 | COD-80 |
| 3 | COD-49 | Make Owner search results page size configurable | 2 | COD-80 |

## 3) Story plans

### COD-80
- AC recap: paging links retain trimmed lastName when provided; no lastName param introduced when empty; works across all controls.
- Affected files: 
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks (by layer):
  - UI: update page-number + first/prev/next/last hrefs to append lastName when model contains non-empty trimmed value.
  - Web: ensure controller puts the effective (trimmed) lastName into model attributes used by template (or reuses existing Owner model).
  - Test: MVC test that requests /owners?lastName=Fra&page=2 keeps filter and model reflects only matching results; verify empty lastName paging.
- Tests per AC: MockMvc controller tests asserting redirect/model/view + generated links (Assumption: assert link params via content match).

### COD-48
- AC recap: add optional city filter; combined lastName startsWith + city match; notFound behavior preserved; paging preserves both params.
- Affected files:
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/main/java/org/springframework/samples/petclinic/owner/Owner.java (only if needed for binding; verify)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks (by layer):
  - UI: add City input bound to Owner.city on findOwners form; ensure form submits via GET like existing.
  - Web: extend processFindForm to trim/normalize city similarly to lastName; include both params in redirects + model.
  - Data: add repository method supporting paging with optional city + lastName (exact vs case-insensitive is an open question; implement per FRD/Jira text).
  - UI (paging): extend COD-80 link logic to preserve city too.
  - Test: MockMvc tests for lastName+city filter, city-only filter (if allowed), and paging retention of both.
- Tests per AC: controller tests verifying 0-result keeps findOwners with field error; multi-page navigation preserves params.

### COD-49
- AC recap: page size default 5; configurable via petclinic.owners.pageSize=10; invalid/non-positive falls back to 5.
- Affected files:
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java
  - (Assumption) a page-size test class exists/needed for valid config (search in repo during implementation).
  - src/main/resources/application.properties (or application.yml) if documenting defaults.
- Sub-tasks (by layer):
  - Config: introduce property binding/reading for petclinic.owners.pageSize with default 5.
  - Web: replace hard-coded PageRequest size with resolved pageSize; guard for invalid values.
  - Test: update/confirm existing invalid-config tests; add test for configured page size.
- Tests per AC: Spring Boot test with @TestPropertySource or properties override; assert Page size impacts totalPages/returned list size.

## 4) Branch & commit strategy
- Plan branch (this PR only): plan/COD-97-implementation-plan.
- Implementation branch (for dev work): feature/COD-97-owner-search.
- Commits: one commit per Story, message prefix with Story key (e.g., "COD-80: preserve lastName in paging links").
- PRs: one code PR for all stories; link back to this plan PR.

## 5) Build / run / health check
- Build: ./mvnw -q -DskipTests=false test (or mvn test).
- Run locally: ./mvnw spring-boot:run and open http://localhost:8080.
- Smoke: Owners > Find Owners, search by last name and/or city, paginate, verify links keep params and results consistent.

## 6) Risks
- Risk: City matching semantics unclear (exact vs case-insensitive vs contains). Owner: Product Owner to confirm; Tech Lead to encode decision.
- Risk: Thymeleaf link generation may duplicate params or encode incorrectly; needs test assertions on rendered HTML. Owner: QA/Developer.
- Risk: Existing tests assume hard-coded page size 5; changing to config may require test refactors. Owner: Developer.

## 7) Definition of Done / approval gates
- Plan PR approved by Tech Lead.
- Design/Architecture links reviewed in Confluence (if required by team).
- Code PR: all stories implemented, unit/MVC tests added, CI green.
- Review: peer review + Tech Lead sign-off.
- Docs: update README/config docs only if property is user-facing.

## 8) Assumptions & open questions
- Assumption: City filter binds to Owner.city and is sent as query param on GET /owners.
- Open: City match behavior (exact/case-insensitive/contains) must be confirmed from FRD/Jira if not explicit.
- Open: Whether paging links should omit empty params (preferred) vs always include with blank.
