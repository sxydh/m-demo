package com.rootdemo.simspoofer;

import de.robv.android.xposed.XposedBridge;

/**
 * 模块日志。默认 INFO，热路径日志在 DEBUG 级别。
 *
 * 可在设置页勾选“调试日志”，phone 进程下次读取配置（≤3s）即切到 DEBUG；
 * 取消勾选回到 INFO。生产环境保持 INFO 即可，不会有逐调用的刷屏。
 */
public final class SimLog {

    public static final int OFF = 0;
    public static final int ERROR = 1;
    public static final int INFO = 2;
    public static final int DEBUG = 3;

    private static final String TAG = "[SimSpoofer] ";

    private static volatile int sLevel = INFO;

    private SimLog() {
    }

    public static void setLevel(int level) {
        sLevel = level;
    }

    public static int level() {
        return sLevel;
    }

    public static void e(String msg) {
        if (sLevel >= ERROR) {
            XposedBridge.log(TAG + "E " + msg);
        }
    }

    public static void i(String msg) {
        if (sLevel >= INFO) {
            XposedBridge.log(TAG + msg);
        }
    }

    public static void d(String msg) {
        if (sLevel >= DEBUG) {
            XposedBridge.log(TAG + "D " + msg);
        }
    }
}
