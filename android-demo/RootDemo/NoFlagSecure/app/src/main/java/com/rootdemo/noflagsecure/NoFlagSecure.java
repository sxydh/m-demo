package com.rootdemo.noflagsecure;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 去除窗口 FLAG_SECURE。
 *
 * 银行类 App（AU 0101 / BOI Mobile 等）会在窗口上设置 FLAG_SECURE，使得
 * 截屏、录屏、adb 镜像（Android Studio / scrcpy）以及 recents 缩略图全部被
 * 系统置黑。这里在 system_server 的 WindowManagerService 里，于窗口添加/重布局
 * 时把 FLAG_SECURE 位清掉——只影响“捕获”，不改变 App 自身渲染。
 *
 * 作用域：只勾 system_server/0（不注入任何目标 App）。
 */
public class NoFlagSecure implements IXposedHookLoadPackage {

    /** android.view.WindowManager.LayoutParams.FLAG_SECURE */
    private static final int FLAG_SECURE = 0x00002000;

    private static final String TAG = "[NoFlagSecure] ";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam == null) {
            return;
        }
        final String pkg = lpparam.packageName;
        if (!"android".equals(pkg) && !"system_server".equals(pkg) && !"system".equals(pkg)) {
            return;
        }
        log("loading in " + pkg);
        hookFlagSecure(lpparam.classLoader);
    }

    private static void hookFlagSecure(ClassLoader cl) {
        final Class<?> wms = findClassQuiet(cl, "com.android.server.wm.WindowManagerService");
        if (wms == null) {
            log("WindowManagerService not found");
            return;
        }
        int installed = 0;
        for (final Method m : wms.getDeclaredMethods()) {
            final String n = m.getName();
            if (!"addWindow".equals(n) && !"relayoutWindow".equals(n)) {
                continue;
            }
            final Class<?>[] pt = m.getParameterTypes();
            int lpIdx = -1;
            for (int i = 0; i < pt.length; i++) {
                if ("android.view.WindowManager$LayoutParams".equals(pt[i].getName())) {
                    lpIdx = i;
                    break;
                }
            }
            if (lpIdx < 0) {
                continue;
            }
            final int idx = lpIdx;
            try {
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam p) {
                        try {
                            final Object lp = p.args[idx];
                            if (lp == null) {
                                return;
                            }
                            final int flags = (Integer) XposedHelpers.getObjectField(lp, "flags");
                            if ((flags & FLAG_SECURE) != 0) {
                                XposedHelpers.setIntField(lp, "flags", flags & ~FLAG_SECURE);
                                log("stripped FLAG_SECURE (" + n + ")");
                            }
                        } catch (Throwable t) {
                            // ignore
                        }
                    }
                });
                installed++;
            } catch (Throwable t) {
                log("hook failed " + n + ": " + t);
            }
        }
        log("hooks installed (" + installed + " on WindowManagerService)");
    }

    private static Class<?> findClassQuiet(ClassLoader cl, String name) {
        try {
            return XposedHelpers.findClass(name, cl);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void log(String msg) {
        XposedBridge.log(TAG + msg);
    }
}
