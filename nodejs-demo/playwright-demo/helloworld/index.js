const { chromium } = require('playwright');

const browser = await chromium.launch({ headless });
const page = await browser.newPage();
await page.goto('https://www.baidu.com/');
