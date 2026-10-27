# COD-97 Implementation Plan — Owner search improvements

## 1) Summary / Scope / Links
- Goal: improve Owner search UX by (a) preserving filters in pagination, (b) adding City filter, (c) configurable page size, (d) better empty-result messaging.
- In-scope stories: COD-80, COD-98, COD-48, COD-49.
- Out of scope: new filters beyond City, i18n overhaul, client-side validation, sorting changes.
- Epic: https://manoj16239.atlassian.net/browse/COD-97
- FRD: Not found via Confluence search (Assumption: FRD page not created or titled differently). Use Jira story descriptions as source of truth until linked.

## 2) Delivery Order
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 2 | — |
| 2 | COD-98 | Improve empty-result messaging for Owner search | 1 | — |
| 3 | COD-48 | Add city filter to Owner search (first slice) | 3 | COD-80 |
| 4 | COD-49 | Make Owner search results page size configurable | 2 | COD-80 |

## 3) Story Plans

### COD-80
- AC recap: paging links keep trimmed lastName when active; no lastName param when unfiltered.
- Affected files (verified):
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI (Thymeleaf): update first/prev/next/last and page-number links to include `lastName` when model indicates active filter.
  - Controller: ensure model contains an “active filter value” for lastName (trimmed), without introducing it for empty case.
  - Tests: add MVC test asserting generated pagination URLs include `lastName=Fra` for filtered results and omit when blank.
- Test types per AC:
  - AC-1/AC-3: Spring MVC slice test asserting view + model + rendered link includes trimmed query param.
  - AC-2: MVC test asserting links do not contain `lastName` when search is unfiltered.

### COD-98
- AC recap: when no results, show message containing searched (trimmed) value; avoid "''" for empty; show targeted error (not all errors).
- Affected files (verified):
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - Controller: replace generic `"not found"` message with contextual message; ensure it uses the trimmed lastName.
  - UI: render field-specific message (e.g., lastName error only) rather than iterating `#fields.allErrors()`.
  - Tests: update/extend existing no-results tests to validate message content and trimming behavior.
- Test types per AC:
  - AC-1/AC-3: MVC test expecting `model().attributeHasFieldErrors("owner","lastName")` and message contains searched value.
  - AC-2: MVC test for blank lastName no-results message does not include quoted empty string.

### COD-48
- AC recap: add optional City filter; results must match lastName prefix + city match; paging preserves both params; no results shows existing notFound behavior.
- Affected files (partially verified):
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - Domain/UI binding: add `city` input bound to `owner.city` in Find Owners form.
  - Repository: add a query method supporting `lastName startsWith` and optional city (exact/contains per decided semantics) (Assumption: exact match on city, case-sensitive/insensitive TBD).
  - Controller: trim city similarly to lastName; incorporate into search and ensure model passes active lastName/city for paging links.
  - UI results: ensure paging links include both `lastName` and `city` when active.
  - Tests: add MVC test for combined filtering and for paging links preserving both params.
- Test types per AC:
  - AC-1: MVC test verifying repository called with expected normalized inputs (via Mockito) and correct view.
  - AC-2: MVC test for no results remains on findOwners with field error.
  - AC-3: MVC test verifying rendered paging URLs include both params.

### COD-49
- AC recap: page size defaults to 5; uses `petclinic.owners.pageSize=10` when configured; invalid/non-positive falls back to 5.
- Affected files (verified):
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/resources/application.properties` (add default property or document)
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java`
- Sub-tasks by layer:
  - Config: introduce `petclinic.owners.pageSize` property; decide whether to set default in code vs properties.
  - Controller: inject property (e.g., `@Value` or `@ConfigurationProperties`) and apply fallback when <= 0.
  - Tests: ensure existing invalid-config test passes; add/adjust test for configured page size=10 and default=5.
- Test types per AC:
  - AC-1: MVC test under default properties expecting controller uses 5.
  - AC-2: Spring test with property override expecting 10.
  - AC-3: existing invalid-config test validates fallback.

## 4) Branch & Commit Strategy
- Plan branch (this PR only): `plan/COD-97-implementation-plan`.
- Implementation branch (per plan): `feature/COD-97-owner-search`.
- Commit rule: one commit per Story, message prefixed with key (e.g., `COD-80: preserve lastName in paging links`).
- Single implementation PR for Epic; keep plan PR separate and merge only after Tech Lead approval.

## 5) Build / Local Deploy / Health Check
- Build: `./mvnw -q clean test` (or `mvn -q clean test`).
- Run: `./mvnw spring-boot:run` and open `http://localhost:8080/`.
- Health checks:
  - Navigate: Owners → Find Owners; search lastName; verify paging retains filters.
  - Verify City filter after COD-48.
  - Verify default/configured page size after COD-49.

## 6) Risks
- Risk: Pagination URL construction becomes error-prone as filters increase (Owner: Tech Lead).
- Risk: City match semantics (exact vs contains, case sensitivity) not explicitly specified may cause test/UX mismatch (Owner: Product Owner).
- Risk: Confluence FRD missing/unlinked could lead to drift (Owner: PM).

## 7) Definition of Done & Approval Gates
- DoD: all ACs met; unit/MVC tests updated and passing; no regressions in existing Owner flows; documentation updated (properties).
- Gates:
  - Plan PR approved by Tech Lead.
  - Design/architecture pages approved in Confluence (if required).
  - Code PR: review + CI green.
  - QA: manual smoke on owner search and paging.
  - Merge via PR only (main protected).

## 8) Assumptions & Open Questions
- Assumption: City matching should be exact (case-insensitive) unless otherwise stated in Jira (needs confirmation).
- Open Q: Should empty lastName (browse-all) ever render `lastName=` in paging links, or omit entirely (AC suggests omit)?
- Open Q: When city is blank after trimming, should it be omitted from paging links similarly?
- Open Q: Where should default pageSize be defined (code vs application.properties) to satisfy AC-1 cleanly?
