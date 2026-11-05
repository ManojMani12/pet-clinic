# PetClinic E2E (Playwright + BDD)

Gherkin scenarios run by [playwright-bdd](https://github.com/vitalets/playwright-bdd) on top of Playwright fixtures.

```
features/            Gherkin scenarios (*.feature), tagged @smoke where relevant
src/fixtures/        Playwright fixtures: page objects + per-scenario `ctx`; exports Given/When/Then
src/steps/           Step definitions (thin; delegate to page objects)
src/pages/           Page objects, one per screen (+ NavBar)
src/support/         Test data builders (unique owner/pet names for parallel safety)
```

## Run

```bash
cd e2e
npm install
npx playwright install chromium

npm test                 # generate specs + run (starts the app via mvnw spring-boot:run)
npm run test:smoke       # @smoke only
npm run test:headed
npm run report           # open HTML report

BASE_URL=http://localhost:8080 npm test   # use an already-running app, skip Maven
```

## Conventions
- Steps never touch `page` locators directly (except trivial shared assertions); add behaviour to a page object.
- Scenarios create their own data via `buildOwner()` / `buildPet()` so they can run in parallel against the shared in-memory H2 DB.
- Don't assert exact owner/vet counts; other scenarios add owners.
- Visits must be dated in the future (`futureDate()`).
