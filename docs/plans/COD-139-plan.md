# COD-139 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- **Epic:** COD-139 (Owner search improvements)
- **Scope:** Preserve search filters across paging, add optional City filter to Owner search, make paging size configurable.
- **Out of scope:** end-user selectable page size UI; non-owner search pagination changes; additional filters beyond City (per COD-125).
- **Jira links:** https://manoj16239.atlassian.net/browse/COD-139 (Epic)
- **FRD (Confluence):** Not found in CodAI space (Assumption: FRD page not created yet).

## 2) Delivery order (blockers first)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-122 | Preserve lastName search across pagination links | 2 | — |
| 2 | COD-125 | Add city filter to Owner search (first slice) | 3 | COD-122 |
| 3 | COD-128 | Make owner search page size configurable and preserved in pagination | 2 | COD-122 |

## 3) Story plans

### COD-122
- **AC recap:** (1) Paging URLs keep `lastName` and results remain filtered; (2) if `lastName` blank, paging URLs omit it; (3) whitespace input is trimmed and the trimmed value is preserved.
- **Affected files (verified):**
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- **Sub-tasks by layer:**
  - Controller: ensure the **effective** trimmed `lastName` is available to the results view model (so Thymeleaf can include it in links).
  - View (Thymeleaf): update first/prev/number/next/last pagination links to include `lastName` when present; omit when empty.
  - Tests: extend MVC tests to assert pagination hrefs include `lastName=Franklin` and omit it for blank search; validate trimming behavior.
- **Tests per AC:**
  - AC-1: MVC test validating rendered pagination links include `page=2&lastName=Franklin` and controller queries with same filter.
  - AC-2: MVC test with blank lastName asserting pagination links do not contain `lastName`.
  - AC-3: MVC test with spaced lastName asserting links contain trimmed value.

### COD-125
- **AC recap:** (1) Add optional `city` filter combined with lastName prefix; preserve `city` on page 2; (2) blank city keeps current behavior; (3) city trimmed and preserved.
- **Affected files (verified):**
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- **Sub-tasks by layer:**
  - View (Find): add `city` input to `findOwners.html` and ensure it is submitted on GET `/owners`.
  - Controller: accept/bind `city`, normalize/trim it, and include it in paging model so ownersList can preserve it; keep existing lastName behaviors.
  - Repository: add a query method to support combined filtering (Assumption: case-insensitive exact match on city, per requirements note) while still supporting pagination.
  - View (Results): extend pagination links to include both `lastName` and `city` when present.
  - Tests: new/extended MVC tests verifying combined filters work, blank city is no-op, and trimmed city is preserved in hrefs.
- **Tests per AC:**
  - AC-1: MVC test (and/or repository-invocation verification via mocking if present) for lastName+city filtering + pagination link preservation.
  - AC-2: MVC test ensuring lastName-only search remains unchanged when city blank.
  - AC-3: MVC test verifying trimming and link preservation for city.

### COD-128
- **AC recap:** (1) default page size remains 5; (2) configurable size (e.g., 10) changes page size; (3) invalid config falls back/clamps (no unhandled exception).
- **Affected files (verified):**
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeConfigTests.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeTooLargeConfigTests.java`
  - `src/main/resources/application.properties` (Assumption: exists and is used for app config; verify before editing)
- **Sub-tasks by layer:**
  - Config: introduce a property (e.g., `petclinic.owners.pageSize`) with default 5 (exact key to confirm from existing test expectations).
  - Controller: read injected config value; validate/clamp to bounds; use it when building `PageRequest`.
  - Tests: ensure the existing page-size configuration tests align with chosen property key and bounds; add/adjust for default and invalid config behavior.
- **Tests per AC:**
  - AC-1: test verifying default remains 5 when property absent.
  - AC-2: test verifying configured size changes paging.
  - AC-3: test verifying invalid config is handled (fallback/clamp).

## 4) Branch & commit strategy
- **Plan branch (this PR):** `plan/COD-139-implementation-plan` (one commit: `COD-139: add implementation plan`).
- **Implementation branch (later, for dev):** `feature/COD-139-owner-search`.
- **Commits (later):** one commit per Story, prefixed with Story key (e.g., `COD-122: preserve lastName in pagination links`).
- **PRs:** one implementation PR for the Epic (all Stories), unless team prefers one PR per Story (Assumption: single PR is acceptable).

## 5) Build / local run / health check
- Build: `./mvnw test` (repo also has Gradle files; use Maven as source of truth unless CI uses Gradle—Assumption).
- Run: `./mvnw spring-boot:run` then open `http://localhost:8080/`.
- Health: verify Find Owners search, paging across results, and filter preservation in browser address bar.

## 6) Risks
- **Risk 1 (Tech Lead):** Query semantics for city (case sensitivity, partial vs exact) may differ from expectation; mitigate by documenting chosen rule and aligning tests to AC/BR.
- **Risk 2 (QA):** Pagination link construction may regress when both filters are present or when values include spaces/special characters; mitigate with MVC rendering assertions and at least one encoded-value test.
- **Risk 3 (Dev):** Introducing config key conflicts with existing tests/property naming; mitigate by reading existing test classes and reusing their expected property keys.

## 7) Definition of Done & approval gates
- Plan PR approved (Tech Lead).
- Design/FRD link available in Confluence (or explicit waiver recorded in Epic).
- Implementation PR passes unit/MVC/integration tests and code review.
- Manual verification: paging preserves active filters; city filter works; page size config works.
- Documentation updated (README/config notes) if new property is introduced.

## 8) Assumptions & open questions
- FRD page was not located in Confluence CodAI; confirm correct FRD location/title and add link.
- Confirm city matching rule: case-insensitive exact match vs partial match; requirements note says assumption is case-insensitive exact.
- Confirm page size config key and upper bound (there are existing *TooLarge* tests; align with them).
