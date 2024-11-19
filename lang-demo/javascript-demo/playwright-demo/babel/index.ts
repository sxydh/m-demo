import {chromium} from 'playwright';
import {astBFS} from './util/ast_bfs';

(async () => {
    const browser = await chromium.launch({
        headless: false,
        executablePath: 'C:/Users/Administrator/AppData/Local/ms-playwright/chromium-1140/chrome-win/chrome.exe'
    });
    const context = await browser.newContext();
    const page = await context.newPage();

    await page.route('**/*.js', async (route, _) => {
        const response = await route.fetch();
        let body = await response.text();
        body += `
            _noitcnuf = (...args) => {
                const body = JSON.stringify(args);
                fetch('http://localhost:3000', {
                    body: body
                }).catch(e => null);
            };`;
        body = astBFS(body);
        await route.fulfill({response, body});
    });

    await page.goto('https://www.baidu.com');

    await page.waitForTimeout(50000);
    await browser.close();
})();
