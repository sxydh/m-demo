// noinspection JSUnresolvedReference,JSUnusedGlobalSymbols

(async () => {
    const {chromium} = require("playwright");
    const browser = await chromium.launch({headless: false});
    const context = await browser.newContext();
    const page = await context.newPage();

    await page.goto("https://www.zhcw.com/kjxx/ssq/");
    await page.waitForLoadState("load", {timeout: 18000});
    await page.waitForTimeout(3000);

    const list = [];
    let pages = 1;
    let pageNum = 1;
    while (pageNum <= pages) {
        const resJson = await page.evaluate(pageNum => {
            return new Promise(resolve => {
                $.ajax({
                    url: "//jc.zhcw.com/port/client_json.php",
                    data: {
                        "transactionType": "10001001",
                        "lotteryId": "1",
                        "issueCount": 0,
                        "startIssue": 0,
                        "endIssue": 0,
                        "startDate": "2010-01-01",
                        "endDate": "2026-01-01",
                        "type": 2,
                        "pageNum": pageNum,
                        "pageSize": 500,
                        "tt": Math.random()
                    },
                    dataType: "jsonp",
                    jsonp: "callback",
                    async: false,
                    success: function (res) {
                        if (res.resCode === "000000") {
                            resolve(res);
                        }
                    }
                });
            });
        }, pageNum);

        list.push(...resJson.data);
        pages = parseInt(resJson.pages);
        pageNum++;
    }

    console.log(list);
    await page.waitForTimeout(999999);
    await browser.close();
})();
