import { BasePage } from './BasePage';

export class FindOwnersPage extends BasePage {
  async open() {
    await this.goto('/owners/find');
  }

  async search(lastName: string) {
    await this.page.locator('#lastName').fill(lastName);
    await this.page.getByRole('button', { name: 'Find Owner' }).click();
  }

  async clickAddOwner() {
    await this.page.getByRole('link', { name: 'Add Owner' }).click();
  }
}
