(async () => {
    const readline = require("readline");
    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });

    let secretId = await new Promise(resolve => {
        rl.question("input secretId: ", (answer) => {
            resolve(answer);
        });
    });

    let secretKey = await new Promise(resolve => {
        rl.question("input secretKey: ", (answer) => {
            resolve(answer);
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

    const fs = require("fs");
    const path = require("path");
    let idCardStr = await new Promise((resolve, reject) => {
        fs.readFile(path.join(__dirname, "id_card.txt"), "utf8", (err, data) => {
            if (err) {
                reject(err);
                return;
            }
            resolve(data);
        });
    });

    idCardStr.split("\n").forEach(idCard => {
        let idCardSplit = idCard.split(",");
        const params = {
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