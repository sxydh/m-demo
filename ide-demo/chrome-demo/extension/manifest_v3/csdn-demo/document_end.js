/* 去除登录 */
let login = document.querySelector(".passport-login-container");
if (login) {
    login.remove();
}

/* 去除关注 */
/* 可以选择 */
let hide = document.querySelector(".hide-article-box.hide-article-pos.text-center");
if (hide) {
    hide.remove();
}
const style = document.createElement('style');
style.innerHTML = `
    #article_content {
        overflow: auto !important;
    }
    
    pre, pre * {
        user-select: text !important;
    }
`;
document.head.appendChild(style);