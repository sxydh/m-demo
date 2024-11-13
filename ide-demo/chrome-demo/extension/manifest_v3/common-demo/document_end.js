/* CSDN */
if (location.href.includes(".csdn.")) {
    /* 去除关注 */
    let hide = document.querySelector(".hide-article-box.hide-article-pos.text-center");
    if (hide) {
        hide.remove();
    }

    /* 可以选择 */
    let style = document.createElement("style");
    style.innerHTML = `
        #article_content {
            overflow: auto !important;
        }
        
        pre, pre * {
            user-select: text !important;
        }`;
    document.head.appendChild(style);

    /* 可以复制 */
    window.addEventListener("copy", event => {
        event.stopImmediatePropagation();
    }, true);
    let signins = document.querySelectorAll(".signin");
    signins.forEach(e => e.remove());

    /* 去除广告 */
    let adRm = () => {
        let selector = "iframe, body > .passport-login-container, body > .passport-login-tip-container";
        let all = document.querySelectorAll(selector);
        all.forEach(e => e.remove());
        console.debug(`try to remove ad`);
    };
    adRm();
    let adSi = setInterval(() => {
        adRm();
    }, 50);
}

/* 才士题库 */
if (location.href.includes(".caishi.")) {
    let style = document.createElement("style");
    style.innerHTML = `
        * {
            cursor: unset !important;
            user-select: unset !important;
        }`;
    document.head.appendChild(style);
}