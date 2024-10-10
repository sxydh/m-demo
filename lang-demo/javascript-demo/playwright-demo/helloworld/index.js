const { chromium } = require('playwright');

const baidu = async () => {
    const browser = await chromium.launch({ headless: false });
    const page = await browser.newPage();
    await page.goto('https://www.baidu.com/');
};

baidu();