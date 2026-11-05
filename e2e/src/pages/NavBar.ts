import { type Page } from '@playwright/test';

export type NavItem = 'Home' | 'Find owners' | 'Veterinarians' | 'Error';

export class NavBar {
  constructor(private readonly page: Page) {}

  async open(item: NavItem) {
    await this.page.locator('.navbar-nav').getByRole('link', { name: item }).click();
  }

  get activeItem() {
    return this.page.locator('.navbar-nav .nav-link.active');
  }
}
