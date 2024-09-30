// https://developer.chrome.com/docs/extensions/reference/api/proxy?hl=zh-cn#description

const config = {
    mode: "fixed_servers",
    rules: {
        singleProxy: {
            scheme: "http",
            host: "127.0.0.1",
            port: 10809
        },
        bypassList: ["localhost"]
    }
};

chrome.proxy.settings.set(
    {
        value: config,
        scope: 'regular'
    },
    function () { }
);
