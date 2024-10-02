var s = document.createElement('script');
s.src = chrome.runtime.getURL('injected.js');
s.onload = function () {
    this.remove();
};
(document.head || document.documentElement).appendChild(s);
console.debug('have injected "injected.js"', new Date().toLocaleTimeString());

// 前程无忧
let interval = setInterval(() => {
    console.debug('interval', interval);
    document.querySelectorAll('.joblist-item .chat').forEach(e => e.remove());
}, 1000);