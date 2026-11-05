import { type Locator, type Page } from '@playwright/test';

export abstract class BasePage {
  constructor(protected readonly page: Page) {}

  get heading(): Locator {
    return this.page.locator('h2').first();
  }

  get fieldErrors(): Locator {
    return this.page.locator('.help-inline');
  }

  async goto(path: string) {
    await this.page.goto(path);
  }
}
