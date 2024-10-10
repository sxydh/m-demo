import { chromium } from 'playwright';

(async () => {
    const browser = await chromium.launch({
        headless: false
    });
    const context = await browser.newContext();
    context.addInitScript({ path: './stealth.min.js' });
    const page = await context.newPage();

    await page.goto('https://taobao.com/');

    await page.waitForTimeout(50000);

    await browser.close();
})();
