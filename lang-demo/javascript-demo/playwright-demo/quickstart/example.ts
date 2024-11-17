import {chromium} from 'playwright';

(async () => {
    const browser = await chromium.launch({headless: false});
    const context = await browser.newContext();
    const page = await context.newPage();

    /* 生命周期回调 */
    // 从上到下优先顺序
    page.on('request', request => {
        console.log(`request url: ${request.url()}`);
    });
    await page.route('https://www.baidu.com/', async (route, request) => {
        const headers = {
            ...request.headers(),
            'My-Header': Date.now().toString()
        };
        const postData = request.postData();
        await route.continue({headers, postData});
    });
    await page.route('**/*.js', async (route, _) => {
        const response = await route.fetch();
        let body = await response.text();
        body = 'console.log("injected script", Date.now());' + body;
        await route.fulfill({response, body});
    });
    page.on('response', async response => {
        const text = await response.text();
        console.log(`response length: ${text.length}`);
    });
    page.on('domcontentloaded', page => {
        console.log(`domcontentloaded: ${page.url()}`);
    });
    page.on('load', page => {
        console.log(`load: ${page.url()}`);
    });
    page.on('close', page => {
        console.log(`close: ${page.url()}`);
    });

    /* 打开目标页面 */
    await page.goto('https://www.baidu.com');

    await page.waitForTimeout(5000);
    await browser.close();
})();
