# COD-146 — Implementation Plan: Owner search improvements

## 1) Summary / scope / links
- Goal: preserve Owner search filters across paging; add optional City filter; make page size configurable.
- In scope: `/owners` search + paging behavior and associated Thymeleaf views + MVC tests.
- Out of scope: sorting; owner details; firstName/telephone filters (explicitly excluded for COD-107).
- Epic: https://manoj16239.atlassian.net/browse/COD-146
- FRD: **Pending** (Confluence parent page id needed in space `CodAI` → `Requirements (FRD)`).

## 2) Delivery order (by blockers)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-88 | Preserve owner last-name filter across pagination links | 2 | — |
| 2 | COD-107 | Add City filter to Owner search (first slice) | 3 | COD-88 |
| 3 | COD-108 | Make Owner search page size configurable (preserved in paging) | 2 | COD-88 |

## 3) Story plans

### COD-88
- AC recap: paging links (numbers + first/prev/next/last) must retain `lastName`; out-of-range `page` redirects to `page=1` and retains `lastName`.
- Affected files (verified):
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: update all pagination `th:href` constructions to include `lastName` query param when present.
  - Web/controller: ensure model exposes current `lastName` consistently for the view; ensure redirect URL on invalid/out-of-range page includes `lastName`.
  - Tests: MVC tests for `/owners?page=N&lastName=Frank` for page-number links and first/prev/next/last controls; add negative test for out-of-range `page` redirect.
- Test types per AC:
  - AC-1/2: `@WebMvcTest` verifying model + rendered links contain `lastName=Frank` and repository called with `Frank`.
  - AC-3: `@WebMvcTest` asserting 3xx redirect to `/owners?page=1&lastName=Frank` (exact path TBD by current controller).

### COD-107
- AC recap: add optional `city` filter; works alone or with `lastName`; no-results returns to find form with error; pagination preserves `city` (+ `lastName` if present).
- Affected files (verified):
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: add City input to `findOwners.html`; ensure entered City is preserved when redisplaying form with error.
  - Web/controller: accept `city` as request param/bound field; include it in model for list + for redirect URLs.
  - Data: extend `OwnerRepository` with a query method supporting city filtering with paging (exact semantics TBD; see open questions).
  - UI paging: extend COD-88 paging link pattern to include both `lastName` and `city` when present.
  - Tests: add MVC tests covering city-only search, combined search, no-results error, and link preservation.
- Test types per AC:
  - AC-1/2: controller tests asserting repository method invoked with expected args and page links include `city=Madison`.
  - AC-3: controller test asserting view `owners/findOwners` and model has an error when no owners found.

### COD-108
- AC recap: default page size = 5; configurable to 10; invalid config uses fallback/clamped value.
- Affected files (verified):
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeConfigTests.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeTooLargeConfigTests.java`
  - (Potential) `src/main/resources/application.properties` (Assumption: property added here)
  - `src/main/resources/templates/owners/ownersList.html` (to preserve effective `size` if introduced as request param)
- Sub-tasks by layer:
  - Config: introduce an application property for owner search page size with default 5 and validated bounds.
  - Web/controller: replace hard-coded `5` page size with resolved/validated configured value.
  - UI: if `size` is exposed as request param (story note says “optionally”), update pagination links to preserve it.
  - Tests: update existing page-size tests (already present) to match finalized property key + validation rules.
- Test types per AC:
  - AC-1/2: config-driven tests (Spring Boot test with overridden properties) verifying page size used.
  - AC-3: invalid/too-large property tests assert fallback/clamp behavior.

## 4) Branch & commit strategy
- Plan branch: `plan/COD-146-implementation-plan` (this PR only).
- Implementation branch (later, by dev): `feature/COD-146-owner-search`.
- Commit strategy (later): one commit per Story, message prefix with Story key (e.g., `COD-88: preserve lastName across paging`).
- PR strategy (later): single implementation PR for the Epic, referencing this plan.

## 5) Build, local deployment, health check
- Build: `./mvnw -q test` (or `mvn -q test`), then `./mvnw -q spring-boot:run`.
- Smoke check:
  - Visit `/owners/find` → search with last name and/or city → verify paging links retain criteria.
  - Hit `/actuator/health` (Assumption: actuator enabled) or confirm app home page renders.

## 6) Risks
- Risk: City matching semantics (exact vs contains/prefix; case sensitivity) unclear → may cause rework. Owner: Product/BA.
- Risk: Paging link construction can become brittle in Thymeleaf if duplicated across controls → regressions likely. Owner: Tech Lead.
- Risk: Page-size bounds not defined; tests already exist and may not match expected clamp/default behavior. Owner: Tech Lead.

## 7) Definition of Done & approval gates
- Plan PR approved by Tech Lead.
- (If required) FRD page created/linked in Confluence and reviewed.
- Implementation PR passes CI (`mvn test`), includes unit/MVC tests per AC, and updates templates.
- Code review + QA sign-off on paging + filtering behavior.
- Docs: update any operator notes for new page-size property (README or application properties comment). (Assumption)

## 8) Assumptions & open questions
- Assumption: paging remains 1-based in URLs (`page=1` is first), as in current controller.
- Open Q (COD-107): confirm city matching rule (exact/contains/prefix) and case sensitivity.
- Open Q (COD-107): confirm City filter should apply to `Owner.city` only (not address/zip).
- Open Q (COD-108): confirm property key name + min/max bounds and whether behavior is clamp or fallback-to-default.
- Open Q (COD-108): confirm whether request parameter `size` is truly in scope (story says optional).
