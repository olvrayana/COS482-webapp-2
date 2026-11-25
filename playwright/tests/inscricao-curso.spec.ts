import { test, expect } from '@playwright/test';

const USER = 'student-0';
const PASSWORD = 'pass-0';
const COURSE_NAME = 'Cocina Moderna'

test.beforeEach(async ({ page }) => {
  await page.goto('/');

  // Espera o cabeçalho renderizar (visitante vê "Log In", usuário logado vê "Log Out")
  const logIn = page.getByText('Log In');
  const logOut = page.getByText('Log Out');
  await expect(logIn.or(logOut)).toBeVisible();

  // Estado inicial: visitante
  if (await logOut.isVisible()) {
    await logOut.click();
  }
  await expect(logIn).toBeVisible();
});

test('Aluno faz login e se inscreve em um curso', async ({ page }) => {
  // Login
  await page.goto('/login');
  await page.locator('input[name="username"]').fill(USER);
  await page.locator('input[name="password"]').fill(PASSWORD);
  await page.locator('button[type="submit"]').click();

  await expect(page.getByRole('heading', { level: 1, name: 'The best way to learn' })).toBeVisible();
  // Inscrição
  await page.getByRole('heading', { level: 2, name: COURSE_NAME, exact: true }).click();
  await expect(page.getByRole('heading', { level: 1, name: COURSE_NAME, exact: true })).toBeVisible();
  await page.getByRole('link', { name: 'Add Course' }).click();

  // Redireciona ao perfil, com o curso em "Inscribed Courses"
  await expect(page).toHaveURL(`/profile/${USER}`);
  const inscribedCourse = page.locator('#inscribed-courses').getByText(COURSE_NAME, { exact: true });
  await inscribedCourse.scrollIntoViewIfNeeded(); // rola a página até o curso aparecer na tela
  await expect(inscribedCourse).toBeVisible();
});

test.afterEach(async ({ page }) => {
  await page.getByText('Log Out').click();
  await expect(page.getByText('Log In')).toBeVisible();
});
