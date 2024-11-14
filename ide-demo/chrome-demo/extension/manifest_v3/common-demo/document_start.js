/* CSDN */
if (location.href.includes(".csdn.")) {
    let style = document.createElement("style");
    style.innerHTML = `
        /* 左侧广告 */
        aside > .box-shadow {
            display: none !important;
        }
        #footerRightAds {
            display: none !important;
        }
        #asideWriteGuide {
            display: none !important;
        }
    
        /* 屏蔽登录 */
        .passport-login-container, .passport-login-tip-container {
            display: none !important;
        }
        .signin {
            display: none !important;
        }
        
        /* 可以选中 */
        p, pre * {
            user-select: text !important;
        }
        #articleSearchTip {
            display: none !important;
        }
            
        /* 屏蔽关注 */
        .hide-article-box.hide-article-pos.text-center {
            display: none !important;
        }
        #article_content {
            overflow: auto !important;
        }`;
    document.head.appendChild(style);

    /* 可以复制 */
    window.addEventListener("copy", event => {
        event.stopImmediatePropagation();
    }, true);
}

/* 才士题库 */
if (location.href.includes(".caishi.")) {
    let style = document.createElement("style");
    style.innerHTML = `
        p {
            cursor: unset !important;
            user-select: unset !important;
        }`;
    document.head.appendChild(style);
}