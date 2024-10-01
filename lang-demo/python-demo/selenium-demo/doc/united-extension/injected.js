(function () {
    console.debug('ready to override XMLHttpRequest', new Date().toLocaleTimeString());

    const originalXMLHttpRequest = window.XMLHttpRequest;

    function newXMLHttpRequest() {
        const xhr = new originalXMLHttpRequest();

        const originalOpen = xhr.open;
        xhr.open = function (method, url) {
            return originalOpen.apply(xhr, arguments);
        };

        const originalSend = xhr.send;
        xhr.send = function (body) {
            xhr.addEventListener('load', function () {
                if (xhr.responseType === 'blob') {
                    return;
                }
                // 前程无忧
                let isQcwy = !xhr.responseURL.includes('https://we.51job.com/api/job/search-pc');
                isQcwy = isQcwy || !xhr.responseURL.includes('&function=');
                if (!isQcwy) {
                    try {
                        let resJson = JSON.parse(xhr.response);
                        let status = resJson.status;
                        if (status !== '1') {
                            return;
                        }
                        let totalCount = resJson.resultbody.job.totalCount;
                        if (totalCount >= 1000 && !location.href.includes('&companySize=')) {
                            return;
                        }
                        fetch(
                            `http://localhost:8080/?url=${encodeURIComponent(location.href)}`,
                            {
                                method: 'POST',
                                body: resJson
                            }
                        ).catch(() => {
                            // NOTHING
                        });
                    } catch (e) {
                        // NOTHING
                    }
                }
            });
            return originalSend.apply(xhr, arguments);
        };
        return xhr;
    }

    window.XMLHttpRequest = newXMLHttpRequest;

    console.debug('finish to override XMLHttpRequest', new Date().toLocaleTimeString());
})();