# COD-132 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: preserve owner search filters across pagination, add optional city filter, and make page size configurable with safe bounds.
- In scope: /owners find flow, OwnerController paging, OwnerRepository query, Thymeleaf findOwners + ownersList templates, MVC tests.
- Out of scope: fuzzy/contains search, sorting, firstName/telephone filters, UI page-size selector.
- Epic: https://manoj16239.atlassian.net/browse/COD-132 | FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/32669697

## 2) Delivery order (blockers first)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-122 | Preserve lastName search across pagination links | 3 | — |
| 2 | COD-125 | Add city filter to Owner search (first slice) | 2 | COD-122 |
| 3 | COD-128 | Make owner search page size configurable and preserved in pagination | 1 | COD-122, COD-125 |

## 3) Story plans

### COD-122
- AC recap: pagination links keep lastName; blank lastName doesn’t emit empty param; whitespace-normalized value is used in links.
- Affected files (verified):
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/resources/templates/owners/ownersList.html
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - Controller: ensure model includes normalized lastName (e.g., add attribute from bound Owner) for template link building.
  - View: update all paging links (first/prev/next/last + numbers) to append lastName when non-empty.
  - Tests: add MVC test asserting rendered paging URLs include lastName and reflect trimmed value.
- Test types per AC: MVC slice tests (MockMvc + view content assertions) for AC-1/2/3.

### COD-125
- AC recap: add optional city filter; blank city preserves current behavior; trimmed city preserved across paging.
- Affected files (verified):
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - View (find): add city input bound to Owner.city (field exists in Owner entity; Assumption until verified in code).
  - Controller: normalize city (null→"", trim) and pass both filters into repository search.
  - Repository: add query method supporting lastName prefix + city match when city provided (derived query or @Query).
  - View (list): extend COD-122 link logic to include city param when non-empty.
- Test types per AC: MVC tests for combined filters, blank city behavior, and trimmed/encoded city propagation.

### COD-128
- AC recap: externalize page size; invalid config handled safely; effective size preserved through paging/redirects.
- Affected files (verified):
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/resources/application.properties
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeConfigTests.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerPageSizeInvalidConfigTests.java
  - src/main/resources/templates/owners/ownersList.html
- Sub-tasks by layer:
  - Config: introduce property (e.g., owners.pageSize) with default; wire into OwnerController (constructor injection).
  - Validation: enforce min/max bound (reuse existing tests’ expectations; adjust tests only if required by FRD).
  - View: preserve size param in paging URLs when non-default is active (only include if request provides it; Assumption).
  - Controller: out-of-range redirect should preserve active filters + size.
- Test types per AC: config tests (already present), MVC tests to verify paging links/redirect include size when applicable.

## 4) Branch & commit strategy
- Plan PR branch (this doc only): plan/COD-132-implementation-plan.
- Implementation branch (for dev work): feature/COD-132-owner-search.
- Commit discipline: one commit per Story (e.g., “COD-122: preserve lastName in paging links”), single code PR referencing Epic.

## 5) Build, local deployment, health check
- Build/test: mvn -q clean test.
- Run: mvn spring-boot:run; verify /owners find flow in browser.
- Health: confirm server starts, home page loads, and owner search paging works for filtered + unfiltered searches.

## 6) Risks
- Risk: Thymeleaf URL building may double-encode or omit params if not modeled consistently (OwnerController/View); Owner: Tech Lead.
- Risk: Repository query for optional city may require @Query to avoid complex derived names/performance; Owner: Backend Dev.

## 7) Definition of Done / approval gates
- DoD: all ACs met; unit/MVC tests added/updated; mvn test green; no regression in existing owner flows.
- Gates: Plan PR approved → (optional) Design note in Confluence → Code PR → Code review → CI green → QA verification → merge.

## 8) Assumptions & open questions
- Assumption: Owner entity has “city” property already (standard PetClinic); verify before updating templates.
- Open Q: Should city matching be exact, prefix, or case-insensitive? FRD only says “matches”; clarify in code PR notes.
- Open Q: Should page size be preserved only via config or also via optional request param “size”? FRD allows “when non-default is in use”; clarify expected mechanism.
