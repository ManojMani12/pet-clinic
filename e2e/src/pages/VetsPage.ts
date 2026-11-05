import { BasePage } from './BasePage';

export class VetsPage extends BasePage {
  async open() {
    await this.goto('/vets.html');
  }

  get rows() {
    return this.page.locator('#vets tbody tr');
  }

  rowFor(vetName: string) {
    return this.rows.filter({ hasText: vetName });
  }

  async goToPage(n: number) {
    await this.page.locator('a[href*="/vets.html?page="]', { hasText: new RegExp(`^${n}$`) }).click();
  }
}
