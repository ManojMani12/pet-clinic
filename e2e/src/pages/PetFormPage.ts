import { BasePage } from './BasePage';
import type { PetData } from '../support/testData';

export class PetFormPage extends BasePage {
  async fill(pet: Partial<PetData>) {
    if (pet.name !== undefined) await this.page.locator('#name').fill(pet.name);
    if (pet.birthDate !== undefined) await this.page.locator('#birthDate').fill(pet.birthDate);
    if (pet.type) await this.page.locator('#type').selectOption({ label: pet.type });
  }

  async submit() {
    await this.page.locator('form button[type=submit]').click();
  }
}
