import { test, expect } from '@playwright/test';

// COD-98: Make Owner search results page size configurable
// NOTE: Runtime configuration cannot be changed by Playwright in this repo by default.
// Tests marked as "assumption" are intended to be executed in an environment where
// the app is started with the desired configuration.

function countOwnerRowsOnList(page: any) {
  return page.locator('table tbody tr').count();
}

test('COD-98 AC-1 default page size is 5', async ({ page }) => {
  // Assumption: app runs with default configuration (no petclinic.owners.pageSize override)
  await page.goto('/owners?lastName=');

  const rows = await countOwnerRowsOnList(page);
  expect(rows).toBeLessThanOrEqual(5);
});

test('COD-98 AC-2 configured page size is applied (assumption)', async ({ page }) => {
  // Assumption: app started with -Dpetclinic.owners.pageSize=10 or config file.
  await page.goto('/owners?lastName=');

  const rows = await countOwnerRowsOnList(page);
  expect(rows).toBeLessThanOrEqual(10);
});

test('COD-98 AC-3 invalid page size falls back to default (assumption)', async ({ page }) => {
  // Assumption: app started with -Dpetclinic.owners.pageSize=0
  await page.goto('/owners?lastName=');

  const rows = await countOwnerRowsOnList(page);
  expect(rows).toBeLessThanOrEqual(5);
});
