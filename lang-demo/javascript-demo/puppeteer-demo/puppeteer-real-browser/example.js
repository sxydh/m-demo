
const { connect } = require("puppeteer-real-browser");

async function test() {

    const { browser, page } = await connect({
        headless: false,
        args: [],
        customConfig: {},
        turnstile: true,
        connectOption: {},
        disableXvfb: false,
        ignoreAllFlags: false
    });
    await page.goto('https://chatgpt.com/');

}

test();