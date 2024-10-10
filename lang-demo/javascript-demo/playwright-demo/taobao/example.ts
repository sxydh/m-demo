import { chromium } from 'playwright';

(async () => {
    const browser = await chromium.launch({
        headless: false
    });
    const context = await browser.newContext();
    // https://github.com/berstend/puppeteer-extra/blob/master/packages/extract-stealth-evasions/readme.md
    // npx extract-stealth-evasions 生成文件 stealth.min.js
    context.addInitScript({ path: './stealth.min.js' });
    const page = await context.newPage();

    await page.goto('https://taobao.com/');

    await page.waitForTimeout(50000);

    await browser.close();
})();
