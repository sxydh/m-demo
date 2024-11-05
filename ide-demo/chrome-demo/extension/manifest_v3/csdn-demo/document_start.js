/* 可以复制 */
(function () {
    const originalAddEventListener = EventTarget.prototype.addEventListener;
    EventTarget.prototype.addEventListener = function (type, listener, options) {
        if (type === 'copy') {
            return;
        }
        return originalAddEventListener.call(this, type, listener, options);
    };
})();