import { test, expect } from '@playwright/test';

// COD-80: Preserve last name filter across Owner results pagination

test('COD-80 AC-1 page-number link preserves lastName filter', async ({ page }) => {
  // Assumption: default PetClinic seed data includes >5 owners starting with "Franklin"
  await page.goto('/owners/find');
  await page.getByLabel('Last name').fill('Fra');
  await page.getByRole('button', { name: 'Find Owner' }).click();

  // Ensure paging is present (multi page)
  const pagination = page.locator('ul.pagination');
  await expect(pagination).toBeVisible();

  // Click page 2 link if present
  const page2 = pagination.getByRole('link', { name: '2' });
  await expect(page2).toBeVisible();
  await page2.click();

  await expect(page).toHaveURL(/\/owners\?page=2.*lastName=Fra/);

  // Assert results are still filtered (table last names should start with Fra)
  // Locators grounded in ownersList.html: table rows under tbody
  const lastNameCells = page.locator('table tbody tr td:nth-child(1)');
  const count = await lastNameCells.count();
  expect(count).toBeGreaterThan(0);
  for (let i = 0; i < count; i++) {
    const txt = (await lastNameCells.nth(i).innerText()).trim();
    expect(txt.startsWith('Franklin') || txt.startsWith('Fra')).toBeTruthy();
  }
});

test('COD-80 AC-2 unfiltered pagination does not add lastName parameter', async ({ page }) => {
  await page.goto('/owners?page=1');

  const pagination = page.locator('ul.pagination');
  await expect(pagination).toBeVisible();

  const hrefs = await pagination.locator('a').evaluateAll((as) =>
    as.map((a) => (a as HTMLAnchorElement).getAttribute('href') || '')
  );

  // No link should introduce lastName when not provided
  for (const href of hrefs) {
    expect(href).not.toContain('lastName=');
  }
});

test('COD-80 AC-3 whitespace trimmed lastName is preserved', async ({ page }) => {
  await page.goto('/owners/find');
  await page.getByLabel('Last name').fill('  Fra  ');
  await page.getByRole('button', { name: 'Find Owner' }).click();

  const pagination = page.locator('ul.pagination');
  await expect(pagination).toBeVisible();

  // Any paging link should contain trimmed value (Fra) not encoded spaces
  const anyLink = pagination.locator('a').first();
  const href = (await anyLink.getAttribute('href')) || '';
  expect(href).toContain('lastName=Fra');
});

test('COD-80 Edge all pagination controls preserve active filters', async ({ page }) => {
  // Use combined filters so we can validate preservation across *all* controls
  await page.goto('/owners/find');

  // City field label is assumed from findOwners.html; if label differs, update locator after QA confirms.
  await page.getByLabel('Last name').fill('Fra');
  await page.getByLabel('City').fill('Madison');
  await page.getByRole('button', { name: 'Find Owner' }).click();

  const pagination = page.locator('ul.pagination');
  await expect(pagination).toBeVisible();

  const hrefs = await pagination.locator('a').evaluateAll((as) =>
    as.map((a) => (a as HTMLAnchorElement).getAttribute('href') || '')
  );

  // BR-2: All paging controls (page numbers, first/prev/next/last) must preserve active params.
  for (const href of hrefs) {
    expect(href).toContain('lastName=Fra');
    expect(href).toContain('city=Madison');
  }
});
