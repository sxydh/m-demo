let style = document.createElement("style");
style.id = "epoh-csdn";
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