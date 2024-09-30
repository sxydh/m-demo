let host = '127.0.0.1';
let port = 10809;
let username = 'username';
let password = 'password';
const config = {
    mode: 'fixed_servers',
    rules: {
        singleProxy: {
            scheme: 'http',
            host: host,
            port: port
        },
        bypassList: ['localhost']
    }
};
chrome.proxy.settings.set(
    {
        value: config,
        scope: 'regular'
    },
    function () { }
);
chrome.webRequest.onAuthRequired.addListener(
    function () {
        return {
            authCredentials: {
                username: username,
                password: password
            }
        };
    },
    {
        urls: ["<all_urls>"]
    },
    ['blocking']
);
console.debug('chrome.proxy.settings.set', config);