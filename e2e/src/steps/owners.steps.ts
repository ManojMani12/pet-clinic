import type { DataTable } from 'playwright-bdd';
import { Given, When, Then, expect } from '../fixtures';
import { buildOwner, type OwnerData } from '../support/testData';

Given('I am on the find owners page', async ({ findOwnersPage }) => {
  await findOwnersPage.open();
});

Given('I am on the add owner page', async ({ ownerFormPage }) => {
  await ownerFormPage.open();
});

/** Creates an owner through the UI and leaves the browser on their details page. */
Given('an owner exists', async ({ ownerFormPage, ownerDetailsPage, ctx }) => {
  ctx.owner = buildOwner();
  await ownerFormPage.open();
  await ownerFormPage.fill(ctx.owner);
  await ownerFormPage.submit();
  await expect(ownerDetailsPage.successMessage).toBeVisible();
});

When('I search for owners with last name {string}', async ({ findOwnersPage }, lastName: string) => {
  await findOwnersPage.search(lastName);
});

When('I click Add Owner', async ({ findOwnersPage }) => {
  await findOwnersPage.clickAddOwner();
});

When('I submit a new owner with valid details', async ({ ownerFormPage, ctx }) => {
  ctx.owner = buildOwner();
  await ownerFormPage.fill(ctx.owner);
  await ownerFormPage.submit();
});

When('I submit a new owner with these details', async ({ ownerFormPage }, table: DataTable) => {
  await ownerFormPage.fill(table.hashes()[0] as Partial<OwnerData>);
  await ownerFormPage.submit();
});

When('I edit the owner changing the city to {string}', async ({ ownerDetailsPage, ownerFormPage }, city: string) => {
  await ownerDetailsPage.editOwner();
  await ownerFormPage.fill({ city });
  await ownerFormPage.submit();
});

When('I go to owners list page {int}', async ({ ownersListPage }, n: number) => {
  await ownersListPage.goToPage(n);
});

Then('the owners list should contain {string}', async ({ ownersListPage }, fullName: string) => {
  await expect(ownersListPage.rows.filter({ hasText: fullName })).toHaveCount(1);
});

Then('the owners list should be paginated', async ({ ownersListPage }) => {
  await expect(ownersListPage.rows.first()).toBeVisible();
  expect(await ownersListPage.paginationLinks.count()).toBeGreaterThan(0);
});

Then('the owners list should not be empty', async ({ ownersListPage }) => {
  await expect(ownersListPage.rows.first()).toBeVisible();
});

Then('the owner details should show {string} as {string}', async ({ ownerDetailsPage }, label: string, value: string) => {
  await expect(ownerDetailsPage.infoValue(label)).toHaveText(value);
});

Then('the owner details should match the entered owner', async ({ ownerDetailsPage, ctx }) => {
  const o = ctx.owner!;
  await expect(ownerDetailsPage.infoValue('Name')).toHaveText(`${o.firstName} ${o.lastName}`);
  await expect(ownerDetailsPage.infoValue('Address')).toHaveText(o.address);
  await expect(ownerDetailsPage.infoValue('City')).toHaveText(o.city);
  await expect(ownerDetailsPage.infoValue('Telephone')).toHaveText(o.telephone);
});
