import {chromium} from 'playwright';
import {astBFS} from './util/ast_bfs';

(async () => {
    // 需要安装浏览器
    // npx playwright install
    const browser = await chromium.launch({
        headless: false
    });
    const context = await browser.newContext();
    await context.addInitScript({path: 'stealth.min.js'});
    const page = await context.newPage();

    await page.route('**/*.js', async (route, _) => {
        const response = await route.fetch();
        let body = await response.text();
        body = astBFS(body);
        // 查询语句
        // window._reffub.filter(e => Object.values(e).some(ve => `${ve}`.includes('')));
        body = `
            _noitcnuf = (keys, values, source) => {
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
                    obj._ecruos = source;
                    window._reffub = window._reffub || [];
                    window._reffub.push(obj);
                }
            };
            ${body}`;
        await route.fulfill({response, body});
    });

    await page.goto('https://www.jd.com/');
})();
