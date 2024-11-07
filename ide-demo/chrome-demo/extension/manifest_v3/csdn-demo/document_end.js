/* 去除登录 */
let rm = () => {
    let logins = document.querySelectorAll(".passport-login-container");
    logins.forEach(e => e.remove());
    console.debug(`try to remove ".passport-login-container"`);
};
rm();
let count = 0;
let si = setInterval(() => {
    if (count > 50) {
        clearInterval(si);
    }
    rm();
    count++;
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