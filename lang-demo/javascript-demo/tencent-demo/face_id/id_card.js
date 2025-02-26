(async () => {
    const readline = require("readline");
    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });

    let secretId = await new Promise(resolve => {
        rl.question("input secretId: \n", (answer) => {
            resolve(answer);
        });
    });

    let secretKey = await new Promise(resolve => {
        rl.question("input secretKey: \n", (answer) => {
            resolve(answer);
        });
    });

    console.log("input id card list: ");
    let idCardList = await new Promise(resolve => {
        let lines = [];
        rl.on("line", line => {
            if (line.trim() === "") {
                resolve(lines);
                return;
            }

            lines.push(line);
        });
    });

    // https://console.cloud.tencent.com/api/explorer?Product=faceid&Version=2018-03-01&Action=IdCardOCRVerification
    const tencentcloud = require("tencentcloud-sdk-nodejs-faceid");
    const client = new tencentcloud.faceid.v20180301.Client({
        credential: {
            // https://console.cloud.tencent.com/cam/capi
            secretId: secretId,
            secretKey: secretKey,
        },
        region: "",
        profile: {
            httpProfile: {
                endpoint: "faceid.tencentcloudapi.com",
            },
        },
    });

    for (let i = 0; i < idCardList.length; i++) {
        let idCard = idCardList[i];
        let idCardSplit = idCard.split(",");
        let params = {
            IdCard: idCardSplit[0],
            Name: idCardSplit[1]
        };
        let res = await client.IdCardVerification(params);
        console.log(res);
        if (res.Result === "0") return;

        new Promise(resolve => {
            setTimeout(resolve, 20);
        });
    }
})();