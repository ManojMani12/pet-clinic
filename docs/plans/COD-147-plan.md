# COD-147 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Scope: owner search UX improvements: (1) preserve filters in pagination, (2) add city as an additional filter, (3) configurable page size.
- Out of scope: new filters beyond city, fuzzy search, sorting controls, UI redesign.
- Epic: https://manoj16239.atlassian.net/browse/COD-147
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/32997379/FRD+-+COD-147+-+Owner+search+improvements (not retrievable via API in this run; assumption link is correct from REQUIREMENTS handoff)
- Repo: https://github.com/ManojMani12/pet-clinic

## 2) Delivery order (by blockers)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-148 | Preserve last-name filter in owner results pagination | 3 | — |
| 2 | COD-151 | Add city filter to owner search (first slice) | 3 | COD-148 |
| 3 | COD-154 | Make owner search page size configurable | 1 | COD-148 |

## 3) Story plans

### COD-148 — Preserve last-name filter in owner results pagination
- AC recap: (1) pagination keeps lastName, (2) no-results stays on find page with error, (3) out-of-bounds redirect keeps lastName.
- Affected files (verified):
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI (Thymeleaf): update paging link hrefs to include query param(s) for the active filter(s) (at least lastName); ensure links include page + lastName.
  - Web/controller: ensure model contains the effective lastName used for search so the view can render it; verify redirect for out-of-bounds includes lastName.
  - Tests: extend MVC tests to cover page navigation retaining lastName and out-of-bounds redirect URL includes lastName.
- Tests per AC:
  - AC-1: MVC test hitting /owners?lastName=…&page=2 and asserting model/view + that generated paging links include lastName (either via HTML content contains or via model attributes used by thymeleaf).
  - AC-2: MVC test /owners?lastName=NoSuchName asserts view owners/findOwners and field error present.
  - AC-3: MVC test /owners?lastName=Fra&page=999 asserts 3xx redirect to /owners?lastName=Fra&page=1.

### COD-151 — Add city filter to owner search (first slice)
- AC recap: (1) lastName + city combined filtering, (2) city-only filtering, (3) no-results for city+lastName remains on find page.
- Affected files (verified):
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html (paging must preserve city too)
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI (Thymeleaf): add city input to findOwners form bound to an Owner field (Owner has address/city/telephone); ensure form submits city as query param.
  - Web/controller: accept city from model attribute/request, normalize (trim) like lastName; pass to repository search; preserve both params on redirect and in paging.
  - Data/repository: add a new repository method to support combined optional filters (e.g., lastName prefix + city match); select match type for city (Assumption: exact match ignoring case OR contains ignoring case; confirm in open questions).
  - Tests: add MVC tests for city-only and combined filtering; verify pagination links preserve both lastName and city.
- Tests per AC:
  - AC-1: MVC test /owners?lastName=Fra&city=Madison asserts returned model owners all match criteria (or verifies repository interaction with expected method via @MockBean if used).
  - AC-2: MVC test /owners?city=Madison asserts filtered results and paging links contain city=Madison.
  - AC-3: MVC test /owners?lastName=Fra&city=NoSuchCity asserts owners/findOwners with error.

### COD-154 — Make owner search page size configurable
- AC recap: (1) default is 5 when unset, (2) configured value (10) applied, (3) invalid non-positive falls back to default.
- Affected files (verified):
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java (currently uses hard-coded pageSize)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeConfigTests.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeTooLargeConfigTests.java
  - src/main/resources/application.properties (Assumption: exists; verify before edit)
- Sub-tasks by layer:
  - Config: introduce/confirm a property key for page size and bind it into OwnerController (or a small @ConfigurationProperties class).
  - Web/controller: replace hard-coded page size with configured value; enforce bounds (non-positive => default; too large => cap or accept per existing tests).
  - Tests: align/update existing page size config tests to the chosen property name and behavior; add coverage for default and invalid.
- Tests per AC:
  - AC-1/2/3: update existing page-size tests to assert pagination size used in PageRequest / results count per page.

## 4) Branch & commit strategy
- Plan branch: plan/COD-147-implementation-plan (this PR only adds the plan document).
- Implementation branch (for later): feature/COD-147-owner-search
- Commit strategy (implementation): one commit per Story, message prefix with Story key (e.g., "COD-148: preserve filters in pagination").
- One code PR for the Epic (unless team prefers one PR per Story; align with repo standards).

## 5) Build, local deployment, health check
- Build/tests: `./mvnw test` (repo also contains Gradle; prefer Maven per project standard in prompt).
- Run locally: `./mvnw spring-boot:run` then open http://localhost:8080
- Smoke checks:
  - Navigate: Owners -> Find Owners; search by last name and verify paging retains filters.
  - Search by city (after COD-151) and verify paging retains city.
  - Verify no regression on owner details pages.

## 6) Risks
- Risk: City matching semantics unclear (exact vs contains vs case sensitivity) may cause rework. Owner: Product Owner to confirm; Dev to document in tests.
- Risk: Pagination link generation in Thymeleaf may be duplicated in multiple places; risk of missing one link type (first/prev/next/last). Owner: Frontend/Fullstack dev.
- Risk: Page size config behavior must match existing tests (too-large handling already has a dedicated test class). Owner: Tech Lead to align expected behavior.

## 7) Definition of Done & approval gates
- DoD: All AC tests passing; new/updated MVC tests added; no regressions in Owner flows; code formatted; documentation updated (property name).
- Gates:
  - Plan PR approved (Tech Lead)
  - Design/decision captured in Confluence (city match rule + page size bounds)
  - Code PR reviewed (peer + Tech Lead)
  - CI green (unit/integration)
  - Deploy to test env / run smoke checks

## 8) Assumptions & open questions
- FRD page could not be fetched via Confluence API (search returned onboarding pages only); assumption the provided FRD link is correct.
- Open: Define city match rule (exact/contains, case sensitivity) for COD-151.
- Open: Confirm page size property key and acceptable max (existing too-large test suggests a cap is expected).
