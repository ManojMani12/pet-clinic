import { test, expect } from '@playwright/test';

// COD-48: Add city filter to Owner search

test('COD-48 AC-1 city + lastName filter restricts results (assumption data)', async ({ page }) => {
  // Assumption: seed data includes owners with lastName starting with Davis and city Madison.
  await page.goto('/owners/find');

  await page.getByLabel('Last name').fill('Davis');
  await page.getByLabel('City').fill('Madison');
  await page.getByRole('button', { name: 'Find Owner' }).click();

  await expect(page).toHaveURL(/\/owners\?.*lastName=Davis.*city=Madison/);

  const rows = page.locator('table tbody tr');
  const rowCount = await rows.count();
  expect(rowCount).toBeGreaterThan(0);

  const lastNames = page.locator('table tbody tr td:nth-child(1)');
  const cities = page.locator('table tbody tr td:nth-child(3)');

  for (let i = 0; i < rowCount; i++) {
    expect(((await lastNames.nth(i).innerText()) || '').trim()).toMatch(/^Davis/i);
    expect(((await cities.nth(i).innerText()) || '').trim()).toBe('Madison');
  }
});

test('COD-48 AC-2 no results stays on find page and shows notFound', async ({ page }) => {
  await page.goto('/owners/find');

  await page.getByLabel('City').fill('NonexistentCity');
  await page.getByRole('button', { name: 'Find Owner' }).click();

  await expect(page).toHaveURL(/\/owners\/find/);

  // Grounded in current PetClinic behavior: validation error contains 'has not been found'
  await expect(page.getByText(/has not been found/i)).toBeVisible();
});

test('COD-48 AC-3 paging preserves lastName and city', async ({ page }) => {
  await page.goto('/owners/find');
  await page.getByLabel('Last name').fill('Fra');
  await page.getByLabel('City').fill('Madison');
  await page.getByRole('button', { name: 'Find Owner' }).click();

  const pagination = page.locator('ul.pagination');
  await expect(pagination).toBeVisible();

  const next = pagination.getByRole('link', { name: /next/i });
  await expect(next).toBeVisible();
  await next.click();

  await expect(page).toHaveURL(/\/owners\?page=\d+.*lastName=Fra.*city=Madison/);

  // Basic assertion: table rows exist
  await expect(page.locator('table tbody tr').first()).toBeVisible();
});

test('COD-48 Edge blank city does not add city param', async ({ page }) => {
  await page.goto('/owners/find');
  await page.getByLabel('Last name').fill('Fra');
  await page.getByRole('button', { name: 'Find Owner' }).click();

  await expect(page).toHaveURL(/\/owners\?.*lastName=Fra/);
  await expect(page).not.toHaveURL(/city=/);
});
