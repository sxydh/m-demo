let interval = setInterval(() => {
    fetch(
        `http://localhost:8080/?url=proxy_config`,
        {
            method: 'POST'
        })
        .then(res => {
            if (!res.ok) {
                console.debug(res.url, res.status, res.statusText);
                return Promise.reject();
            }
            return res.json();
        })
        .then(data => {
            console.debug('proxy_config', data);
            let host = data.host;
            let port = data.port;
            let username = data.username;
            let password = data.password;
            if (host && port) {
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
                    function (details, callbackFn) {
                        callbackFn({
                            authCredentials: {
                                username: username,
                                password: password
                            }
                        });
                    },
                    {
                        urls: ["<all_urls>"]
                    },
                    ['asyncBlocking']
                );
                console.debug('chrome.proxy.settings.set', config);
            }
        })
        .catch(e => {
            console.debug(e);
        });
}, 2000);