
import { puppeteer } from 'puppeteer';

(async () => {
    const browser = await puppeteer.launch();
    const page = await browser.newPage();
    await page.goto('https://www.baidu.com');
    await page.waitForTimeout(5000);
    await browser.close();
})();