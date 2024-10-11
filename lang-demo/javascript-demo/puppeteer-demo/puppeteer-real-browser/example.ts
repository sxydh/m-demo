
import { connect } from 'puppeteer-real-browser';

(async () => {

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
    // https://www.browserscan.net/bot-detection
    await page.goto('https://chatgpt.com/');

})();