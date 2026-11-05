import { Given, When, Then, expect } from '../fixtures';
import type { NavItem } from '../pages/NavBar';

Given('I am on the home page', async ({ homePage }) => {
  await homePage.open();
});

When('I open the home page', async ({ homePage }) => {
  await homePage.open();
});

When('I click the {string} menu item', async ({ navBar }, item: string) => {
  await navBar.open(item as NavItem);
});

Then('I should see the heading {string}', async ({ page }, heading: string) => {
  await expect(page.locator('h2').first()).toContainText(heading);
});

Then('I should see the field error {string}', async ({ page }, text: string) => {
  await expect(page.locator('.help-inline').filter({ hasText: text }).first()).toBeVisible();
});

Then('I should see the success message {string}', async ({ ownerDetailsPage }, text: string) => {
  await expect(ownerDetailsPage.successMessage).toContainText(text);
});
