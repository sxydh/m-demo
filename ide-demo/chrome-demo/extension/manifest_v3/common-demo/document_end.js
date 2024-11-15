/* CSDN */
if (location.href.includes(".csdn.")) {
    let style = document.createElement("style");
    style.innerHTML = `
        /* 顶部广告 */
        .toolbar-advert,
        .ad-wrap {
            display: none !important;
        }
    
        /* 左侧广告 */
        #footerRightAds,
        #asideWriteGuide {
            display: none !important;
        }
        aside > .box-shadow {
            display: none !important;
        }
            
        /* 右侧广告 */
        #recommendAdBox,
        #kp_box_www_swiper {
            display: none !important;
        }
        #rightAside .programmer1Box {
            display: none !important;
        }
            
        /* 其它广告 */
        #articleSearchTip {
            display: none !important;
        }
        .csdn-side-toolbar,
        .tool-active-list,
        .passport-login-container,
        .passport-login-tip-container,
        .signin,
        .hide-article-box.hide-article-pos.text-center {
            display: none !important;
        }
    
        /* 选择文字 */
        p, 
        span,
        pre * {
            user-select: text !important;
        }
        `;
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
        /* 可以选中 */
        p,
        div {
            cursor: text !important;
            user-select: text !important;
        }`;
    document.head.appendChild(style);
}

/* 百度文库 */
if (location.href.includes("//baike.")) {
    let style = document.createElement("style");
    style.innerHTML = `
        /* 右侧广告 */
        .unionAd_InMop {
            display: none !important;
        }
        /* 底部广告 */
        #J-bottom-recommend-wrapper {
            display: none !important;
        }
        `;
    document.head.appendChild(style);
}