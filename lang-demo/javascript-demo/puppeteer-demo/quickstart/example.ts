import puppeteer, {HTTPRequest, HTTPResponse} from 'puppeteer';

(async () => {
    const browser = await puppeteer.launch({
        headless: false,
        defaultViewport: null,
        args: ["--start-maximized"]
    });
    const page = await browser.newPage();
    await page.setRequestInterception(true);

    page.on('request', (req: HTTPRequest) => {
        console.log(`request: ${req.url().substring(0, 50)}...`);
        req.continue();
    });
    page.on('response', async (res: HTTPResponse) => {
        console.log(`response: ${res.headers()['content-type']}`);
    });
    page.on('domcontentloaded', () => {
        console.log(`domcontentloaded`);
    });
    page.on('load', () => {
        console.log(`load`);
    });

    await page.goto('https://www.baidu.com');
})();