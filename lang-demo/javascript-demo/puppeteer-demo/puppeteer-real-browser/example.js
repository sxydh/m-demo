
const { connect } = require("puppeteer-real-browser");

async function test() {

    const { browser, page } = await connect({
        headless: false,
        args: ["--start-maximized"],
        customConfig: {},
        turnstile: true,
        connectOption: {
            defaultViewport: null
        },
        disableXvfb: false,
        ignoreAllFlags: false
    });
    await page.goto('https://chatgpt.com/');

}

test();