# COD-99 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: preserve active Owner search filters during pagination; add City filter; externalize Owner list page size.
- In scope: Owners find + results flow only (/owners), Thymeleaf templates, repository query, MVC tests.
- Out of scope: global pagination framework; per-user page-size selector UI; new filters beyond City.
- Epic: https://manoj16239.atlassian.net/browse/COD-99
- FRD: Not found in Confluence space `CodAI` (Assumption: FRD page not created yet).

## 2) Delivery order (blockers first)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-80 | Preserve last name filter across Owner results pagination | 1 | — |
| 2 | COD-48 | Add city filter to Owner search (first slice) | 2 | COD-80 |
| 3 | COD-98 | Make Owner search results page size configurable | 1 | COD-80 |

## 3) Story plans

### COD-80
- AC recap: paging links must keep `lastName` when provided; no `lastName` param when unfiltered; whitespace-trimmed value is used.
- Affected files (verified):
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: update pagination anchors to append `lastName` query param only when model has non-empty trimmed lastName.
  - Controller: ensure model exposes the effective/trimmed `lastName` for view rendering (if not already).
  - Tests: MVC test asserts pagination links include `lastName` for filtered search and omit for unfiltered.
- Test types per AC:
  - AC-1/AC-2: MockMvc + view content assertions for generated paging URLs.
  - AC-3: MockMvc request with whitespace input, follow model/view to ensure trimmed value used for links.

### COD-48
- AC recap: add optional City filter; no-results returns to findOwners w/ existing notFound behavior; pagination preserves both `lastName` + `city`.
- Affected files (verified):
  - `src/main/resources/templates/owners/findOwners.html`
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI: add `city` input on Find Owners; keep binding consistent with existing Owner form object (Assumption: use `*{city}` since `Owner` has address fields).
  - Controller: trim `city` similarly to `lastName`; pass both filters through to repository; add both to model for paging links.
  - Repository: add new paged query method supporting lastName prefix + city match (open question: exact vs startsWith vs contains; follow story wording “matches” -> exact unless clarified).
  - UI (results): extend pagination link building (from COD-80) to include `city` when present.
- Test types per AC:
  - AC-1: MockMvc verifying repository invoked with both filters (via @MockBean) OR integration slice (if existing pattern) validating model list.
  - AC-2: MockMvc expecting `findOwners` view and `lastName` field error `notFound` (existing behavior) when results empty.
  - AC-3: MockMvc asserting paging URLs include both `lastName` and `city`.

### COD-98
- AC recap: default page size 5; configurable via `petclinic.owners.pageSize`; invalid/non-positive falls back to 5.
- Affected files (verified):
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
  - `src/main/resources/application.properties` (Assumption: exists; verify before change)
- Sub-tasks by layer:
  - Config: introduce property `petclinic.owners.pageSize` with default 5; validate >0 (fallback).
  - Controller: replace hard-coded `pageSize=5` with injected/configured value.
  - Tests: add/adjust tests for default, configured size (e.g., @TestPropertySource), and invalid size fallback.
- Test types per AC:
  - AC-1/AC-2: MockMvc verifying pagination model (`totalPages`, `currentPage`) and list size assumptions with seeded data (follow existing test style).
  - AC-3: config-focused test class already exists (`OwnerControllerPageSizeInvalidConfigTests`)—extend/assert fallback behavior.

## 4) Branch & commit strategy
- Plan branch: `plan/COD-99-implementation-plan` (this PR only contains the plan).
- Delivery branch for code: `feature/COD-99-owner-search`.
- Commit approach for implementation PR: one commit per Story: `COD-80 ...`, `COD-48 ...`, `COD-98 ...`.
- One implementation PR targeting `main`.

## 5) Build / local run / health check
- Build: `./mvnw -q -DskipTests=false test` (or `./mvnw test`).
- Run: `./mvnw spring-boot:run` then open `http://localhost:8080`.
- Verify manually: Owners → Find Owners; search with lastName; page through results; repeat with city; verify URLs keep params.

## 6) Risks
- Risk: City matching semantics unclear (exact vs prefix vs contains, case sensitivity). Owner: Product/BA.
- Risk: Thymeleaf link building for optional params can be error-prone (double-encoding, missing params). Owner: Tech Lead.
- Risk: Tests may rely on fixed page size; changing to configurable can cause brittle assertions. Owner: QA/Dev.

## 7) Definition of Done / approval gates
- Plan PR approved by Tech Lead.
- Design documented in Confluence (FRD link added when available).
- Implementation PR: code review, tests passing, no regression in existing owner flows.
- Evidence: screenshots or notes of manual paging verification + automated MVC tests.
- Docs: update README/config docs if property is introduced.

## 8) Assumptions & open questions
- FRD page not found in Confluence `CodAI`; confirm location/title and add link later.
- City filter semantics: interpret “matches” as exact match (case-insensitive?) unless clarified.
- Ensure paging links preserve all active filters (at minimum lastName + city) without introducing params when empty.
