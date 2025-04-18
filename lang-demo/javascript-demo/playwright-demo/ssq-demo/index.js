// noinspection JSUnresolvedReference,JSUnusedGlobalSymbols

const initServer = async () => {

};

const initData = async () => {
    const {chromium} = require("playwright");
    const browser = await chromium.launch({headless: true});
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

    const db = require("better-sqlite3")("index.db", {});
    db.pragma("journal_mode = WAL");
    db.exec("CREATE TABLE IF NOT EXISTS t_ssq (id INTEGER PRIMARY KEY AUTOINCREMENT, d TEXT, r TEXT, r2 TEXT, r3 TEXT, r4 TEXT, r5 TEXT, r6 TEXT, b TEXT)");
    db.exec("DELETE FROM t_ssq WHERE 1 = 1");

    const insert = db.prepare("INSERT INTO t_ssq (d, r, r2, r3, r4, r5, r6, b) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    for (let i = 0; i < list.length; i++) {
        const ele = list[i];
        const frontSplit = ele.seqFrontWinningNum.split(" ");
        insert.run(ele.openTime, frontSplit[0], frontSplit[1], frontSplit[2], frontSplit[3], frontSplit[4], frontSplit[5], ele.seqBackWinningNum);
    }

    await browser.close();
};

(async () => {
    await initServer();
    await initData();
})();
