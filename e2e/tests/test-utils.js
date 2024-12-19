

export async function login_steps(page) {
    await page.getByLabel('Username').fill('admin');
    await page.getByLabel('Password').fill('admin');
    await page.getByLabel('Sign in').click();
}

export async function open_alert_page_and_filter(page, filter) {
    await page.goto('/wizard/AlertRules');
    // Wait for rules are loaded
    await page.waitForTimeout(1000);
    await page.getByPlaceholder('Filter alert rules').fill(filter);
    // Wait for filter is applied
    await page.waitForTimeout(500);
}

export async function fill_field_condition(page, input, option, value) {
    await page.getByRole('button', { name: 'add_circle' }).first().click();
    await page.waitForTimeout(200);
    await page.locator('#field-input').fill(input);
    await page.waitForTimeout(200);
    await page.getByText('arrow_drop_down').nth(2).click();
    await page.getByRole('option', { name: option }).click();
    await page.locator('#value').fill(value);
    await page.waitForTimeout(200);
}