import { firefox as impl } from 'playwright';

// http://www.site-digger.com/html/weibo/2022/0810/881.html
(async () => {
    const browser = await impl.launch({
        headless: false
    });
    const context = await browser.newContext();
    // https://github.com/berstend/puppeteer-extra/blob/master/packages/extract-stealth-evasions/readme.md
    // stealth.min.js <= npx extract-stealth-evasions
    // https://www.browserscan.net/bot-detection
    context.addInitScript({ path: 'stealth.min.js' });
    const page = await context.newPage();

    await page.goto('https://taobao.com/');

    await page.waitForTimeout(50000);

    await browser.close();
})();
