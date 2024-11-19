import {chromium} from 'playwright';
import {astBFS} from './util/ast_bfs';
import {server} from "./util/http_server";

(async () => {
    server.listen(3000);

    const browser = await chromium.launch({
        headless: false,
        executablePath: 'C:/Users/Administrator/AppData/Local/ms-playwright/chromium-1140/chrome-win/chrome.exe'
    });
    const context = await browser.newContext();
    const page = await context.newPage();

    await page.route('**/*.js', async (route, _) => {
        const response = await route.fetch();
        let body = await response.text();
        body = astBFS(body);
        body = `
            _noitcnuf = (...args) => {
                window._reffub = window._reffub || [];
                window._reffub.push([...args]);
            };
            ${body}`;
        await route.fulfill({response, body});
    });

    await page.goto('https://www.baidu.com');
})();
