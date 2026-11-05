import { BasePage } from './BasePage';

export class HomePage extends BasePage {
  async open() {
    await this.goto('/');
  }
}
