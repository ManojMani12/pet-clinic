import { type Locator } from '@playwright/test';
import { BasePage } from './BasePage';

export class OwnerDetailsPage extends BasePage {
  get successMessage() {
    return this.page.locator('#success-message');
  }

  /** Value cell of the owner info table, by its row header (Name, Address, City, Telephone). */
  infoValue(label: string): Locator {
    return this.page
      .locator('table')
      .first()
      .locator('tr', { has: this.page.locator('th', { hasText: label }) })
      .locator('td');
  }

  petRow(petName: string): Locator {
    return this.page.locator('tr').filter({ has: this.page.locator(`dd:text-is("${petName}")`) });
  }

  visitDescriptions(petName: string): Locator {
    return this.petRow(petName).locator('table.table-condensed tr td:nth-child(2)');
  }

  async editOwner() {
    await this.page.getByRole('link', { name: 'Edit Owner' }).click();
  }

  async addNewPet() {
    await this.page.getByRole('link', { name: 'Add New Pet' }).click();
  }

  async addVisit(petName: string) {
    await this.petRow(petName).getByRole('link', { name: 'Add Visit' }).click();
  }

  async editPet(petName: string) {
    await this.petRow(petName).getByRole('link', { name: 'Edit Pet' }).click();
  }
}
