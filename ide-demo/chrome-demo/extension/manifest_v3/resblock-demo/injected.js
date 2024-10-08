(function () {
    console.log('ready to override XMLHttpRequest', new Date().toLocaleTimeString());

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
                if (xhr.responseType !== 'blob') {
                    fetch(`http://localhost:8080/?url=${encodeURIComponent(xhr.responseURL)}`,
                        {
                            method: 'POST',
                            body: xhr.response
                        }
                    ).catch(() => {
                        // NOTHING
                    });
                }
            });
            return originalSend.apply(xhr, arguments);
        };
        return xhr;
    }

    window.XMLHttpRequest = newXMLHttpRequest;

    console.log('finish to override XMLHttpRequest', new Date().toLocaleTimeString());
})();