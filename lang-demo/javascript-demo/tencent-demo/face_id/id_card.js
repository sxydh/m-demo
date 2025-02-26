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
    let idCardStr = await new Promise(resolve => {
        let lines = "";
        rl.on("line", line => {
            if (line.trim() === "") {
                resolve(lines);
                return;
            }

            lines += line;
        });
    });

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

    idCardStr.split("\n").forEach(idCard => {
        let idCardSplit = idCard.split(",");
        let params = {
            IdCard: idCardSplit[0],
            Name: idCardSplit[1]
        };
        client.IdCardVerification(params).then(
            data => {
                console.log(data);
            },
            error => {
                console.error(error);
            }
        );

        new Promise(resolve => {
            setTimeout(resolve, 20);
        });
    });
})();