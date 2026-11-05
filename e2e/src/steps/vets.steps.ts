import { Given, When, Then, expect } from '../fixtures';

Given('I am on the veterinarians page', async ({ vetsPage }) => {
  await vetsPage.open();
});

When('I go to vets page {int}', async ({ vetsPage }, n: number) => {
  await vetsPage.goToPage(n);
});

Then('the vets table should not be empty', async ({ vetsPage }) => {
  await expect(vetsPage.rows.first()).toBeVisible();
});

Then('the vet {string} should have specialty {string}', async ({ vetsPage }, vet: string, specialty: string) => {
  // The seed list spans two pages; look on the first page, then the second.
  if ((await vetsPage.rowFor(vet).count()) === 0) await vetsPage.goToPage(2);
  await expect(vetsPage.rowFor(vet)).toContainText(specialty);
});

Then('the vets list should be paginated', async ({ page }) => {
  await expect(page.locator('a[href*="/vets.html?page="]').first()).toBeVisible();
});
