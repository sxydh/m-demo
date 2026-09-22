// hook_libnesec.js
// 参考 tig3rhu《他趣APP脱壳》：
// 网易易盾 libnesec.so 会自建线程做反调试/反 Frida 检测，导致 hook 时秒崩。
// 这里 hook libc 的 pthread_create，凡是 libnesec.so 创建的线程，把其入口回调
// 替换为空函数，使检测线程“空转”，从而保住进程供 dump_dex.js 脱壳。
function hook_pthread_create() {
    // Frida 17：Module 的静态查找方法已移除，改用 Process.getModuleByName(...).getExportByName(...)
    var pt_create_func = Process.getModuleByName("libc.so").getExportByName("pthread_create");
    if (pt_create_func === null) {
        console.log("[!] pthread_create not found");
        return;
    }
    var replaced = {};
    Interceptor.attach(pt_create_func, {
        onEnter: function (args) {
            try {
                var start_routine = args[2];
                if (start_routine.isNull()) return;
                var m = Process.findModuleByAddress(start_routine);
                if (m === null) return;
                if (m.name.indexOf("libnesec") === -1) return;
                var key = start_routine.toString();
                if (replaced[key]) return;
                replaced[key] = true;
                try {
                    Interceptor.replace(start_routine, new NativeCallback(function () {
                        console.log("[+] libnesec thread callback neutralized @ " + key);
                        return ptr(0);
                    }, "pointer", ["pointer"]));
                } catch (e) {
                    console.log("[!] replace failed @" + key + " : " + e);
                }
            } catch (e) {
                console.log("[!] onEnter error: " + e);
            }
        },
        onLeave: function (retval) {}
    });
    console.log("[+] pthread_create hook installed");
}

setImmediate(hook_pthread_create);
