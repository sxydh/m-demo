setInterval(() => {
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
            if (host && port) {
                // https://developer.chrome.com/docs/extensions/reference/api/proxy?hl=zh-cn#description
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
                console.debug('chrome.proxy.settings.set', config);
            }
        })
        .catch(e => {
            console.debug(e);
        });
}, 2000);