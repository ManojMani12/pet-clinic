import type { DataTable } from 'playwright-bdd';
import { Given, When, Then, expect, type PageFixtures } from '../fixtures';
import { buildPet, futureDate, type PetData } from '../support/testData';

/** Adds a pet via the UI; leaves the browser on the owner page (success) or the form (validation error). */
const addPet = async (
  { ownerDetailsPage, petFormPage }: Pick<PageFixtures, 'ownerDetailsPage' | 'petFormPage'>,
  pet: Partial<PetData>,
) => {
  await ownerDetailsPage.addNewPet();
  await petFormPage.fill(pet);
  await petFormPage.submit();
};

Given('the owner has a pet', async ({ ownerDetailsPage, petFormPage, ctx }) => {
  ctx.pet = buildPet();
  await addPet({ ownerDetailsPage, petFormPage }, ctx.pet);
  await expect(ownerDetailsPage.petRow(ctx.pet.name)).toBeVisible();
});

When('I add a pet with these details', async ({ ownerDetailsPage, petFormPage, ctx }, table: DataTable) => {
  const row = table.hashes()[0] as unknown as PetData;
  ctx.pet = { ...row };
  await addPet({ ownerDetailsPage, petFormPage }, row);
});

When('I add a pet of type {string}', async ({ ownerDetailsPage, petFormPage, ctx }, type: string) => {
  ctx.pet = buildPet({ type });
  await addPet({ ownerDetailsPage, petFormPage }, ctx.pet);
});

When('I add a pet with the same name', async ({ ownerDetailsPage, petFormPage, ctx }) => {
  await addPet({ ownerDetailsPage, petFormPage }, buildPet({ name: ctx.pet!.name }));
});

When('I rename the pet to {string}', async ({ ownerDetailsPage, petFormPage, ctx }, name: string) => {
  await ownerDetailsPage.editPet(ctx.pet!.name);
  await petFormPage.fill({ name });
  await petFormPage.submit();
  ctx.pet = { ...ctx.pet!, name };
});

When('I add a visit with description {string}', async ({ ownerDetailsPage, visitFormPage, ctx }, description: string) => {
  await ownerDetailsPage.addVisit(ctx.pet!.name);
  await visitFormPage.fill(futureDate(), description);
  await visitFormPage.submit();
});

Then('the owner should have a pet {string}', async ({ ownerDetailsPage }, name: string) => {
  await expect(ownerDetailsPage.petRow(name)).toBeVisible();
});

Then('the owner should have the added pet', async ({ ownerDetailsPage, ctx }) => {
  const row = ownerDetailsPage.petRow(ctx.pet!.name);
  await expect(row).toBeVisible();
  await expect(row.locator('dd').nth(2)).toHaveText(ctx.pet!.type);
});

Then('the pet should have a visit {string}', async ({ ownerDetailsPage, ctx }, description: string) => {
  await expect(ownerDetailsPage.visitDescriptions(ctx.pet!.name).filter({ hasText: description })).toHaveCount(1);
});
