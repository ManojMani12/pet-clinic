# COD-46 Implementation Plan — Owner search improvements

## 1) Summary / scope / links
- Goal: preserve owner search criteria across pagination in Owner results.
- In scope: Story COD-47 (pagination links include lastName when present; tests updated).
- Out of scope: adding new search fields or changing match semantics.
- Epic: https://manoj16239.atlassian.net/browse/COD-46
- Story: https://manoj16239.atlassian.net/browse/COD-47
- FRD/Source: (Assumption) Gap Analysis page referenced in Epic description; direct Confluence FRD not found via search.

## 2) Delivery order
| Order | Story | Title | Points | Blocked by |
|---:|---|---|---:|---|
| 1 | COD-47 | Preserve owner last-name filter in pagination links | 1 | — |

## 3) Story plans

### COD-47
- AC recap: paging links (first/prev/next/last/numbered) must include `lastName` when filtered; must omit when blank.
- Affected files (verified):
  - `src/main/resources/templates/owners/ownersList.html`
  - `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
  - `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`
- Sub-tasks by layer:
  - UI (Thymeleaf): update pagination URLs to pass `lastName` when present (prefer Thymeleaf param syntax so encoding is safe).
  - Web (Controller): ensure model exposes current `lastName` consistently for list view (if not already; use existing `owners.getLastName()` binding).
  - Tests: add WebMvc test asserting rendered pagination links include `lastName` for filtered searches; add test verifying blank search links do not include `lastName`.
- Test types per AC:
  - AC-1/AC-2: WebMvc/MockMvc HTML content assertions for hrefs containing `lastName=Fran` + page param.
  - AC-3: WebMvc/MockMvc assertion that hrefs do not contain `lastName=` when lastName is blank.

## 4) Branch & commit strategy
- Plan branch: `plan/COD-46-implementation-plan` (this PR).
- Implementation branch (for later): `feature/COD-46-owner-search`.
- Commit strategy (later): one commit per Story, message prefixed with key (e.g., `COD-47: preserve lastName in pagination links`).
- One code PR to `main` for the Epic’s stories.

## 5) Build, local deployment, health check
- Build: `./mvnw clean test` (or `mvn clean test`).
- Run: `./mvnw spring-boot:run`.
- Verify:
  - Navigate: Owners → Find owners → search lastName producing multiple pages.
  - Click pagination; confirm URL keeps `lastName` and results remain filtered.

## 6) Risks
- Risk: URL encoding issues for special characters in `lastName` if concatenated manually; mitigate by using Thymeleaf URL param syntax. Owner: Tech Lead.
- Risk: Test brittleness due to HTML changes/whitespace; mitigate by asserting key substrings and limiting scope. Owner: QA/Dev.

## 7) Definition of Done & approval gates
- Plan PR approved by Tech Lead.
- Story meets all ACs with automated tests passing.
- Code PR approved (peer review), CI green.
- No regressions in existing OwnerControllerTests.
- Documentation: update any relevant developer notes if paging behavior changes.

## 8) Assumptions & open questions
- Assumption: The only criteria to preserve is `lastName` (per COD-47 scope).
- Open question: Should paging links include `lastName` even when it is the empty string (current AC says omit when blank) — proceed with omitting.
- Open question: Confluence FRD page link for COD-46 not located via API search; confirm exact FRD URL/title if required for compliance.
