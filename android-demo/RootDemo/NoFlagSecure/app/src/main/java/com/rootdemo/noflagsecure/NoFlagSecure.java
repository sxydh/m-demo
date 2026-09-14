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
 * 银行类 App（AU 0101 / BOI Mobile 等）会在窗口上设置 FLAG_SECURE，使截屏、录屏、
 * adb 镜像（Android Studio / scrcpy）以及 recents 缩略图被系统置黑。本模块在
 * system_server 的 WindowManagerService 里，于窗口添加/重布局时清掉 FLAG_SECURE
 * 位——只影响“捕获”，不改变 App 自身渲染。
 *
 * 作用域：只勾 system/0（system_server），**不注入任何目标 App**。
 *
 * 日志：默认只打印加载/安装/清除动作；逐次清除可开 {@link #DEBUG}。
 */
public class NoFlagSecure implements IXposedHookLoadPackage {

    /** android.view.WindowManager.LayoutParams.FLAG_SECURE */
    private static final int FLAG_SECURE = 0x00002000;

    private static final String TAG = "[NoFlagSecure] ";

    /** 置 true 时逐次打印“stripped FLAG_SECURE”，排查用；默认只打印首次。 */
    private static final boolean DEBUG = false;

    private static volatile boolean sLoggedFirstStrip = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam == null) {
            return;
        }
        try {
            final String pkg = lpparam.packageName;
            // system_server 的包名在不同版本/形态下为 android / system_server / system
            if (!"android".equals(pkg) && !"system_server".equals(pkg) && !"system".equals(pkg)) {
                return;
            }
            log("loading in " + pkg);
            hookFlagSecure(lpparam.classLoader);
        } catch (Throwable t) {
            log("handleLoadPackage failed: " + t);
        }
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
                                if (!sLoggedFirstStrip) {
                                    sLoggedFirstStrip = true;
                                    log("stripped FLAG_SECURE (" + n + ")");
                                } else {
                                    logd("stripped FLAG_SECURE (" + n + ")");
                                }
                            }
                        } catch (Throwable t) {
                            // 单个窗口失败不影响其它
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

    private static void logd(String msg) {
        if (DEBUG) {
            XposedBridge.log(TAG + msg);
        }
    }
}
