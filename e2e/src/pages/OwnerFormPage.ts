import { BasePage } from './BasePage';
import type { OwnerData } from '../support/testData';

export class OwnerFormPage extends BasePage {
  async open() {
    await this.goto('/owners/new');
  }

  async fill(owner: Partial<OwnerData>) {
    for (const [field, value] of Object.entries(owner)) {
      await this.page.locator(`#${field}`).fill(value);
    }
  }

  async submit() {
    await this.page.locator('form#add-owner-form button[type=submit]').click();
  }
}
