import { defineConfig, devices } from '@playwright/test';
import { defineBddConfig } from 'playwright-bdd';

const baseURL = process.env.BASE_URL ?? 'http://localhost:8080';
// Set BASE_URL to test an already-running instance; otherwise Maven starts the app.
const startApp = !process.env.BASE_URL;
const mvnw = process.platform === 'win32' ? '.\\mvnw.cmd' : './mvnw';

const testDir = defineBddConfig({
  features: 'features/**/*.feature',
  steps: ['src/steps/**/*.ts', 'src/fixtures/index.ts'],
});

export default defineConfig({
  testDir,
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL,
    locale: 'en-US',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: startApp
    ? {
        command: `${mvnw} -q spring-boot:run`,
        cwd: '..',
        url: baseURL,
        reuseExistingServer: !process.env.CI,
        timeout: 240_000,
      }
    : undefined,
});
