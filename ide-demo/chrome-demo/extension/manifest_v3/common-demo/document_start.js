/* CSDN */
if (location.href.includes(".csdn.")) {
    /* 去除广告 */
    let adRm = () => {
        let selector = "#asideWriteGuide, #recommendAdBox, #footerRightAds, iframe, body > .passport-login-container, body > .passport-login-tip-container";
        let all = document.querySelectorAll(selector);
        all.forEach(e => e.remove());
        console.debug(`try to remove ad`);
    };
    adRm();
    let adSi = setInterval(() => {
        adRm();
    }, 10);
}