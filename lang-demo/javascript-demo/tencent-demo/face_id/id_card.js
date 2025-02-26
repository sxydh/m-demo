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
    client.IdCardVerification({}).then(
        data => {
            console.log(data);
        },
        error => {
            console.error(error);
        }
    );
})();