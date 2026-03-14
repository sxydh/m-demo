Java.perform(function () {
    console.log("");
    console.log("======================================");
    console.log("[+] Frida 环境验证通过！");
    console.log("======================================");

    var Activity = Java.use("android.app.Activity");

    // 明确指定 Hook 只有一个参数的 onCreate 方法
    Activity.onCreate.overload('android.os.Bundle').implementation = function (bundle) {
        console.log("[*] 成功拦截到界面启动: " + this.getComponentName().getClassName());
        this.onCreate(bundle);
    };
});