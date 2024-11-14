/* CSDN */
if (location.href.includes(".csdn.")) {
    let style = document.createElement("style");
    style.innerHTML = `
        /* 左侧广告 */
        #footerRightAds, #asideWriteGuide {
            display: none !important;
        }
        aside > .box-shadow {
            display: none !important;
        }
            
        /* 右侧广告 */
        #recommendAdBox {
            display: none !important;
        }
        #rightAside .programmer1Box {
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
        }
            
        /* 屏蔽工具栏 */
        .csdn-side-toolbar {
            display: none !important;
        }
        .tool-active-list {
            display: none !important;
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