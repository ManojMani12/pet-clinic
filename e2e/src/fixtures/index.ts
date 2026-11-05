import { test as base, createBdd } from 'playwright-bdd';
import { HomePage } from '../pages/HomePage';
import { FindOwnersPage } from '../pages/FindOwnersPage';
import { OwnersListPage } from '../pages/OwnersListPage';
import { OwnerFormPage } from '../pages/OwnerFormPage';
import { OwnerDetailsPage } from '../pages/OwnerDetailsPage';
import { PetFormPage } from '../pages/PetFormPage';
import { VisitFormPage } from '../pages/VisitFormPage';
import { VetsPage } from '../pages/VetsPage';
import { NavBar } from '../pages/NavBar';
import type { OwnerData, PetData } from '../support/testData';

/** Per-scenario state shared between steps (fresh for every scenario). */
export interface ScenarioContext {
  owner?: OwnerData;
  pet?: PetData;
}

export type PageFixtures = {
  homePage: HomePage;
  findOwnersPage: FindOwnersPage;
  ownersListPage: OwnersListPage;
  ownerFormPage: OwnerFormPage;
  ownerDetailsPage: OwnerDetailsPage;
  petFormPage: PetFormPage;
  visitFormPage: VisitFormPage;
  vetsPage: VetsPage;
  navBar: NavBar;
  ctx: ScenarioContext;
};

export const test = base.extend<PageFixtures>({
  homePage: ({ page }, use) => use(new HomePage(page)),
  findOwnersPage: ({ page }, use) => use(new FindOwnersPage(page)),
  ownersListPage: ({ page }, use) => use(new OwnersListPage(page)),
  ownerFormPage: ({ page }, use) => use(new OwnerFormPage(page)),
  ownerDetailsPage: ({ page }, use) => use(new OwnerDetailsPage(page)),
  petFormPage: ({ page }, use) => use(new PetFormPage(page)),
  visitFormPage: ({ page }, use) => use(new VisitFormPage(page)),
  vetsPage: ({ page }, use) => use(new VetsPage(page)),
  navBar: ({ page }, use) => use(new NavBar(page)),
  ctx: async ({}, use) => use({}),
});

export const { Given, When, Then } = createBdd(test);
export { expect } from '@playwright/test';
