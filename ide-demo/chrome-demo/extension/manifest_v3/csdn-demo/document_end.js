/* 去除登录 */
let plcRm = () => {
    let logins = document.querySelectorAll(".passport-login-container");
    logins.forEach(e => e.remove());
    console.debug(`try to remove ".passport-login-container"`);
};
plcRm();
let plcSiCount = 0;
let plcSi = setInterval(() => {
    if (plcSiCount > 50) {
        clearInterval(plcSi);
    }
    plcRm();
    plcSiCount++;
}, 100);

/* 去除关注 */
/* 可以选择 */
let hide = document.querySelector(".hide-article-box.hide-article-pos.text-center");
if (hide) {
    hide.remove();
}
let style = document.createElement("style");
style.innerHTML = `
    #article_content {
        overflow: auto !important;
    }
    
    pre, pre * {
        user-select: text !important;
    }
`;
document.head.appendChild(style);

/* 可以复制 */
window.addEventListener("copy", event => {
    event.stopImmediatePropagation();
}, true);
let signins = document.querySelectorAll(".signin");
signins.forEach(e => e.remove());

/* 去除广告 */
let adRm = () => {
    let logins = document.querySelectorAll(".toolbar-advert");
    logins.forEach(e => e.remove());
    console.debug(`try to remove ".toolbar-advert"`);
};
adRm();
let adSiCount = 0;
let adSi = setInterval(() => {
    if (adSiCount > 50) {
        clearInterval(adSi);
    }
    adRm();
    adSiCount++;
}, 100);