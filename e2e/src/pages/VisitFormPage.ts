import { BasePage } from './BasePage';

export class VisitFormPage extends BasePage {
  async fill(date: string, description: string) {
    await this.page.locator('#date').fill(date);
    await this.page.locator('#description').fill(description);
  }

  async submit() {
    await this.page.locator('form button[type=submit]').click();
  }
}
