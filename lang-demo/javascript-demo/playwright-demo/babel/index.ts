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
        body = astBFS(body);
        body = `
            _noitcnuf = (keys, values) => {
                const obj = {};
                let isPush = false;
                for (let i = 0; i < keys.length; i++) {
                    if (!['boolean', 'string', 'number'].includes(typeof values[i])) {
                        continue;
                    }
                    obj[keys[i]] = values[i];
                    isPush = true;
                }
                if (isPush) {
                    window._reffub = window._reffub || [];
                    window._reffub.push(obj);
                }
            };
            ${body}`;
        await route.fulfill({response, body});
    });

    await page.goto('https://www.baidu.com');
})();
