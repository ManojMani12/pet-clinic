# COD-167 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: improve owner search UX by preserving filters across pagination, adding City as an optional filter, and making page size configurable.
- In scope: OwnerController / OwnerRepository, owners find + list Thymeleaf templates, MVC tests.
- Out of scope: new entities/schema changes, adding additional filters beyond City, UI redesign.
- Epic: COD-167 (Owner search improvements)
- FRD: https://manoj16239.atlassian.net/wiki/spaces/CodAI/pages/33292289/FRD+-+COD-167+-+Owner+search+improvements (not retrievable via API; Assumption based on Jira handoff)

## 2) Delivery order (by blockers)
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-168 | Preserve last-name filter across owner results pagination | 2 | — |
| 2 | COD-169 | Add City filter to Owner search (first slice) | 3 | COD-168 |
| 3 | COD-170 | Make Owner results page size configurable | 2 | COD-168 |

## 3) Story plans

### COD-168
- AC recap: (1) paging keeps lastName; (2) paging works without lastName; (3) preserve normalized (trimmed) lastName.
- Affected files (verified):
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI: Update pagination link th:hrefs to include lastName when present (using model attribute), keep current behavior when absent.
  - Web/controller: Ensure model includes the normalized lastName used for searching (e.g., model.addAttribute("lastName", lastName)).
  - Tests: Extend OwnerControllerTests to validate that ownersList view receives lastName and that paging links (rendered HTML) include lastName param when provided.
- Test types per AC:
  - AC-1: MockMvc GET /owners?lastName=Fra&page=2 verifies model contains lastName="Fra" and response contains "lastName=Fra" in paging URLs.
  - AC-2: MockMvc GET /owners?page=2 verifies response does not contain "lastName=" and paging still renders.
  - AC-3: MockMvc GET /owners?lastName=%20Franklin%20&page=2 verifies model lastName="Franklin" and rendered URLs use trimmed value.

### COD-169
- AC recap: (1) lastName prefix AND city filter; (2) no results shows not-found on find form; (3) paging preserves lastName+city.
- Affected files (verified):
  - src/main/resources/templates/owners/findOwners.html
  - src/main/resources/templates/owners/ownersList.html
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - UI: Add City input field bound to Owner.city (Owner has city field; verified in Owner.java presence is an Assumption until checked).
  - Data: Add repository method for combined filtering, e.g. findByLastNameStartingWithAndCity(...) with Pageable (exact vs ignore-case to align with FRD; open question).
  - Web/controller: In processFindForm, normalize lastName and city (trim), apply combined query when city present else keep existing query; pass city + lastName into model for ownersList.
  - UI paging: Extend paging links to include both lastName and city when present.
  - Tests: Add MVC tests for lastName+city filtering branches and pagination parameter preservation.
- Test types per AC:
  - AC-1: MockMvc GET /owners?lastName=Fra&city=Madison asserts repository called with both filters (via @MockBean verification) OR asserts model listOwners is expected stub.
  - AC-2: MockMvc GET /owners?lastName=Fra&city=Nope asserts model has field error and view owners/findOwners.
  - AC-3: MockMvc GET /owners?lastName=Fra&city=Madison&page=2 asserts rendered paging URLs contain both params.

### COD-170
- AC recap: (1) default page size 5; (2) configurable to N; (3) paging keeps search criteria.
- Affected files (verified):
  - src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java
  - src/main/resources/application.properties (exists in typical PetClinic; Assumption until verified)
  - src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
- Sub-tasks by layer:
  - Config: Introduce property (e.g., petclinic.owners.page-size) with default 5; wire into OwnerController via @Value or @ConfigurationProperties.
  - Web/controller: Replace hard-coded pageSize=5 in findPaginatedForOwnersLastName with injected value; ensure unchanged behavior when property absent.
  - Tests: Add test using @TestPropertySource to set page size and validate controller uses it (e.g., verify repository pageable size or model totalPages with stubbed Page).
- Test types per AC:
  - AC-1: Existing tests should continue passing; add explicit assertion that pageable size equals 5 when no property set.
  - AC-2: New test with property set to N verifies pageable size N.
  - AC-3: Reuse COD-168/COD-169 paging-link tests to ensure parameters preserved while page size changes.

## 4) Branch & commit strategy
- Plan branch: plan/COD-167-implementation-plan (this PR).
- Implementation branch (for dev work): feature/COD-167-owner-search.
- Commit strategy (implementation PR): one commit per Story (COD-168, COD-169, COD-170) prefixed with story key; keep tests in same commit as feature.
- One code PR for the Epic; link back to this plan PR.

## 5) Build, local deployment, health check
- Build/tests: ./mvnw test (or ./gradlew test if using Gradle; repo includes both — choose one standard for CI).
- Run locally: ./mvnw spring-boot:run then open http://localhost:8080.
- Manual checks:
  - Navigate: Owners → Find owners; search lastName; paginate next/prev; ensure filter persists.
  - Add city filter and repeat.
  - Confirm configured page size changes results per page.

## 6) Risks
- Risk 1 (Tech Lead): Query semantics for City matching (case sensitivity, partial vs exact) unclear; may cause rework.
- Risk 2 (QA): Pagination link assertions via rendered HTML can be brittle; keep selectors stable and assert minimal strings.
- Risk 3 (Dev): Combined repository method naming may explode if more filters added later; may need Specification-based approach (future).

## 7) Definition of Done & approval gates
- DoD: ACs met; unit/MVC tests added/updated; no regression in existing Owner flows; code reviewed; docs updated (if properties added).
- Gates:
  - Plan PR approved (this PR)
  - Design/FRD confirmation in Confluence
  - Code PR created + reviewed
  - CI green (tests)
  - Optional smoke test on deployed env

## 8) Assumptions & open questions
- Assumption: FRD URL in Jira handoff is correct; Confluence API search did not locate FRD page by title.
- Assumption: Owner entity has city field (typical PetClinic); confirm in src/main/java/.../owner/Owner.java.
- Open Q: City matching should be exact vs case-insensitive vs prefix; align repository method accordingly.
- Open Q: Property name and bounds for page size; confirm with PO/Tech Lead.
