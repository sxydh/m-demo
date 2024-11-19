import {chromium} from 'playwright';

(async () => {
    const browser = await chromium.launch({headless: false});
    const context = await browser.newContext();
    const page = await context.newPage();

    await page.route('**/*.js', async (route, _) => {
        const response = await route.fetch();
        let body = await response.text();
        body = 'console.log("injected script", Date.now());' + body;
        await route.fulfill({response, body});
    });

    await page.goto('https://www.baidu.com');

    await page.waitForTimeout(50000);
    await browser.close();
})();
