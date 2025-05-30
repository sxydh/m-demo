import {chromium} from 'playwright';
import {astBFS} from './util/ast_bfs';

(async () => {
    // 需要安装浏览器
    // npx playwright install
    const browser = await chromium.launch({
        headless: false,
        args: ['--start-maximized']
    });
    const context = await browser.newContext({
        viewport: null
    });
    await context.addInitScript({path: 'stealth.min.js'});
    const page = await context.newPage();

    await page.route('**/*.js', async (route, _) => {
        const response = await route.fetch();
        let body = await response.text();
        body = astBFS(body);
        // 查询语句
        // window._hcraes = (text) => {
        //     window._reffub.filter(e => Object.values(e).some(ve => `${ve}`.includes(text))).forEach(e => {
        //         console.log(e);
        //         console.log(e._ecruos);
        //     });
        // };
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
                    const flag = ' at ';
                    let stack = new Error().stack;
                    let index = stack.indexOf(flag);
                    index = stack.indexOf(flag, index + flag.length);
                    stack = stack.substring(index, stack.indexOf(flag, index + flag.length));
                    obj._ecruos = stack.trim();
                    window._reffub = window._reffub || [];
                    window._reffub.push(obj);
                }
            };
            ${body}`;
        await route.fulfill({response, body});
    });

    await page.goto('https://www.jd.com/');
})();