import { BasePage } from './BasePage';

export class OwnersListPage extends BasePage {
  get rows() {
    return this.page.locator('#owners tbody tr');
  }

  get paginationLinks() {
    return this.page.locator('a[href*="/owners?page="]');
  }

  async openOwner(fullName: string) {
    await this.page.locator('#owners').getByRole('link', { name: fullName }).click();
  }

  async goToPage(n: number) {
    await this.page.locator('a[href*="/owners?page="]', { hasText: new RegExp(`^${n}$`) }).click();
  }
}
