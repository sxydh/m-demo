// https://stackoverflow.com/questions/8939467/chrome-extension-to-read-http-response
(function () {
    var XHR = XMLHttpRequest.prototype;

    var open = XHR.open;
    var send = XHR.send;
    var setRequestHeader = XHR.setRequestHeader;

    XHR.open = function (method, url) {
        this._method = method;
        this._url = url;
        this._requestHeaders = {};
        return open.apply(this, arguments);
    };

    XHR.setRequestHeader = function (header, value) {
        this._requestHeaders[header] = value;
        return setRequestHeader.apply(this, arguments);
    };

    XHR.send = function (body) {
        console.log(`url = ${this._url}`);
        console.log(`body = ${body}`);
        this.addEventListener('load', function () {
            if (this.responseType != 'blob' && this.responseText) {
                console.log(`response = ${this.responseText}`);
            }
        });
        return send.apply(this, arguments);
    };
})();