package com.rootdemo.simspoofer;

import android.net.NetworkCapabilities;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * SIM/网络环境伪装模块（Xposed / Vector）。
 *
 * 设计要点：**不注入目标 App**，只改“数据源”：
 *   - phone 进程（com.android.phone）：telephony/binder 服务返回值（ICCID/IMSI/号码/
 *     运营商/订阅/网络类型）；
 *   - system_server：连通性（WiFi/Ethernet/VPN → 蜂窝）、以及把 adb_enabled /
 *     development_settings_enabled 对普通 App 返回 0。
 * 目标 App 只是通过正常 IPC 拿到已被伪造的数据，自身进程内没有 Xposed 痕迹。
 *
 * 作用域：com.android.phone/0 + system/0（system_server）。**不要勾目标 App**。
 *
 * 日志：默认 INFO（见 {@link SimLog}）；设置页可开 DEBUG 打印逐调用日志。
 * 健壮性：各 hook 分组彼此隔离，单点失败只记 ERROR，不影响其它 hook。
 *
 * 注意（能力边界）：本模块伪造的是“App 读到的值”，无法伪造网络侧身份
 * （真正的 A 号码 / SMS 发送 / 运营商注册），因此过不了需要真实 SIM 的
 * 短信绑定校验；这类问题不在本模块职责范围内。
 */
public class SimSpoofer implements IXposedHookLoadPackage {

    private static final String PHONE_PACKAGE = "com.android.phone";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam == null) {
            return;
        }
        try {
            final String pkg = lpparam.packageName;
            if ("android".equals(pkg) || "system_server".equals(pkg) || "system".equals(pkg)) {
                logi("loading in system_server: " + pkg);
                safe(() -> hookConnectivityAsync(lpparam.classLoader), "connectivity");
                safe(() -> hookSettings(lpparam.classLoader), "settings");
                return;
            }
            if (!PHONE_PACKAGE.equals(pkg)) {
                return;
            }
            logi("loading in phone process: " + pkg);
            final ClassLoader cl = lpparam.classLoader;
            final Class<?> subInfo = findClass(cl, "android.telephony.SubscriptionInfo");
            if (subInfo == null) {
                loge("SubscriptionInfo not found, abort");
                return;
            }
            safe(() -> hookPhoneSubInfo(cl, subInfo), "phoneSubInfo");
            safe(() -> hookPhoneInterfaceManager(cl), "phoneInterfaceManager");
            safe(() -> hookSubscriptionController(cl, subInfo), "subscriptionController");
            safe(() -> hookSmsSend(cl), "smsSend");
        } catch (Throwable t) {
            loge("handleLoadPackage failed: " + t);
        }
    }

    private interface Task {
        void run();
    }

    /** 单个 hook 分组失败不影响整包加载。 */
    private static void safe(Task task, String name) {
        try {
            task.run();
        } catch (Throwable t) {
            loge("hook group '" + name + "' failed: " + t);
        }
    }

    /**
     * 诊断：记录真正的短信发送调用（不改变行为，INFO 级）。
     *
     * Android 16 上 ISms 的实现类是 com.android.internal.telephony.SmsController；
     * IccSmsInterfaceManager 已不在 ISms binder 路径上，只作旧版本兜底。
     */
    private static void hookSmsSend(ClassLoader cl) {
        Class<?> c = findClassQuiet(cl, "com.android.internal.telephony.SmsController");
        if (c == null) {
            c = findClassQuiet(cl, "com.android.internal.telephony.IccSmsInterfaceManager");
        }
        if (c == null) {
            logi("SMS manager class not found");
            return;
        }
        final String cn = c.getSimpleName();
        for (final Method m : c.getDeclaredMethods()) {
            final String n = m.getName();
            if (!(n.startsWith("sendText") || n.startsWith("sendMultipartText")
                    || n.startsWith("sendData"))) {
                continue;
            }
            hookMethod(m, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    logi("SMS SEND " + cn + "#" + n + " uid=" + uid() + " args="
                            + java.util.Arrays.toString(p.args));
                }
            });
        }
    }

    // ---------------------------------------------------------------------
    // PhoneSubInfoController: ICCID / IMSI / Line1 / Operator
    // ---------------------------------------------------------------------

    private static void hookPhoneSubInfo(ClassLoader cl, Class<?> subInfo) {
        final Class<?> c = findClass(cl, "com.android.internal.telephony.PhoneSubInfoController");
        if (c == null) {
            return;
        }
        hookString(c, "getIccSerialNumber", () -> ProfileStore.get(cl).iccid);
        hookString(c, "getIccSerialNumberForSubscriber", () -> ProfileStore.get(cl).iccid);
        hookString(c, "getSimSerialNumber", () -> ProfileStore.get(cl).iccid);
        hookString(c, "getSimSerialNumberForSubscriber", () -> ProfileStore.get(cl).iccid);

        hookString(c, "getSubscriberId", () -> ProfileStore.get(cl).imsi);
        hookString(c, "getSubscriberIdForSubscriber", () -> ProfileStore.get(cl).imsi);

        hookString(c, "getLine1Number", () -> ProfileStore.get(cl).line1);
        hookString(c, "getLine1NumberForSubscriber", () -> ProfileStore.get(cl).line1);
        hookString(c, "getMsisdn", () -> ProfileStore.get(cl).line1);
        hookString(c, "getMsisdnForSubscriber", () -> ProfileStore.get(cl).line1);

        hookString(c, "getSimOperator", () -> ProfileStore.get(cl).simOperator);
        hookString(c, "getSimOperatorForSubscriber", () -> ProfileStore.get(cl).simOperator);
        hookString(c, "getSimOperatorName", () -> ProfileStore.get(cl).operatorName);
        hookString(c, "getSimOperatorNameForSubscriber", () -> ProfileStore.get(cl).operatorName);
        hookString(c, "getNetworkOperator", () -> ProfileStore.get(cl).networkOperator);
        hookString(c, "getNetworkOperatorForSubscriber", () -> ProfileStore.get(cl).networkOperator);
        hookString(c, "getNetworkOperatorName", () -> ProfileStore.get(cl).networkOperatorName);
        hookString(c, "getNetworkOperatorNameForSubscriber", () -> ProfileStore.get(cl).networkOperatorName);
    }

    // ---------------------------------------------------------------------
    // PhoneInterfaceManager: SIM state / card presence / modem count
    // ---------------------------------------------------------------------

    private static void hookPhoneInterfaceManager(ClassLoader cl) {
        Class<?> c = findClass(cl, "com.android.phone.PhoneInterfaceManager");
        if (c == null) {
            c = findClass(cl, "com.android.internal.telephony.PhoneInterfaceManager");
        }
        if (c == null) {
            return;
        }
        hookInt(c, "getSimState", () -> ProfileStore.get(cl).simState);
        hookInt(c, "getSimStateForSubscriber", () -> ProfileStore.get(cl).simState);
        hookInt(c, "getSimStateForSlotIndex", () -> ProfileStore.get(cl).simState);
        hookBool(c, "hasIccCard", () -> ProfileStore.get(cl).hasIcc);
        hookInt(c, "getActiveModemCount", () -> 1);
        hookInt(c, "getSupportedModemCount", () -> 1);
        hookInt(c, "getNetworkType", () -> ProfileStore.get(cl).networkType);
        hookInt(c, "getNetworkTypeForSubscriber", () -> ProfileStore.get(cl).networkType);
        hookInt(c, "getDataNetworkType", () -> ProfileStore.get(cl).networkType);
        hookInt(c, "getDataNetworkTypeForSubscriber", () -> ProfileStore.get(cl).networkType);
        hookInt(c, "getVoiceNetworkType", () -> ProfileStore.get(cl).networkType);
        hookInt(c, "getVoiceNetworkTypeForSubscriber", () -> ProfileStore.get(cl).networkType);
    }

    // ---------------------------------------------------------------------
    // SubscriptionController: subscription list / counts / defaults
    // ---------------------------------------------------------------------

    private static void hookSubscriptionController(ClassLoader cl, Class<?> subInfo) {
        Class<?> c = findClass(cl, "com.android.internal.telephony.subscription.SubscriptionManagerService");
        if (c == null) {
            c = findClass(cl, "com.android.internal.telephony.subscription.SubscriptionController");
        }
        if (c == null) {
            c = findClass(cl, "com.android.internal.telephony.SubscriptionController");
        }
        if (c == null) {
            return;
        }
        hookSubscriptionList(c, "getActiveSubscriptionInfoList", subInfo, cl);
        hookSubscriptionList(c, "getAccessibleSubscriptionInfoList", subInfo, cl);
        hookSubscriptionSingle(c, "getActiveSubscriptionInfo", subInfo, cl);
        hookInt(c, "getActiveSubInfoCount", () -> 1);
        hookInt(c, "getActiveSubInfoCountMax", () -> 1);

        hookInt(c, "getDefaultSubId", () -> ProfileStore.get(cl).subscriptionId);
        hookInt(c, "getDefaultDataSubId", () -> ProfileStore.get(cl).subscriptionId);
        hookInt(c, "getDefaultVoiceSubId", () -> ProfileStore.get(cl).subscriptionId);
        hookInt(c, "getDefaultSmsSubId", () -> ProfileStore.get(cl).subscriptionId);
        hookInt(c, "getSlotIndex", () -> 0);
        hookInt(c, "getSlotIndexForSubId", () -> 0);
    }

    // ---------------------------------------------------------------------
    // system_server: 把 WiFi/以太网/VPN 伪装成蜂窝（过“必须蜂窝网络”门禁）
    // ---------------------------------------------------------------------

    private static Class<?> connectivityServiceClass(ClassLoader cl) {
        for (String n : new String[]{
                "android.net.connectivity.com.android.server.ConnectivityService",
                "com.android.server.connectivity.ConnectivityService",
                "com.android.server.ConnectivityService"}) {
            Class<?> c = findClassQuiet(cl, n);
            if (c != null) {
                return c;
            }
        }
        try {
            Class<?> sm = findClassQuiet(cl, "android.os.ServiceManager");
            Object svc = XposedHelpers.callStaticMethod(sm, "getService", "connectivity");
            if (svc != null && !svc.getClass().getName().contains("BinderProxy")) {
                logd("ConnectivityService via ServiceManager: " + svc.getClass().getName());
                return svc.getClass();
            }
        } catch (Throwable t) {
            loge("ConnectivityService via ServiceManager failed: " + t);
        }
        return null;
    }

    private static Class<?> findClassQuiet(ClassLoader cl, String name) {
        try {
            return XposedHelpers.findClass(name, cl);
        } catch (Throwable t) {
            return null;
        }
    }

    private static volatile boolean sConnHooked = false;

    private static void hookConnectivityAsync(final ClassLoader cl) {
        try {
            Class<?> sm = findClassQuiet(cl, "android.os.ServiceManager");
            if (sm != null) {
                for (final Method m : sm.getDeclaredMethods()) {
                    if (!m.getName().equals("addService")) {
                        continue;
                    }
                    Class<?>[] pt = m.getParameterTypes();
                    if (pt.length < 2 || !IBinder.class.isAssignableFrom(pt[1])) {
                        continue;
                    }
                    logd("hooking ServiceManager#addService (" + pt.length + " args)");
                    hookMethod(m, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam p) {
                            if (sConnHooked) {
                                return;
                            }
                            try {
                                Object name = p.args[0];
                                Object svc = p.args[1];
                                if ("connectivity".equals(name) && svc != null) {
                                    logi("captured connectivity service: "
                                            + svc.getClass().getName());
                                    doHookConnectivity(svc.getClass());
                                }
                            } catch (Throwable t) {
                                loge("addService capture err: " + t);
                            }
                        }
                    });
                }
            }
        } catch (Throwable t) {
            loge("hook addService failed: " + t);
        }
        Thread poll = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 180 && !sConnHooked; i++) {
                    try {
                        Class<?> cs = connectivityServiceClass(cl);
                        if (cs != null) {
                            doHookConnectivity(cs);
                            return;
                        }
                    } catch (Throwable t) {
                        loge("connectivity poll err: " + t);
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }, "SimSpooferConnectivity");
        poll.setDaemon(true);
        poll.start();
    }

    private static void doHookConnectivity(Class<?> cs) {
        if (sConnHooked) {
            return;
        }
        sConnHooked = true;
        logi("hooking ConnectivityService class = " + cs.getName());
        for (Method m : cs.getDeclaredMethods()) {
            try {
                if ("getNetworkCapabilities".equals(m.getName())
                        && m.getReturnType() == NetworkCapabilities.class) {
                    hookMethod(m, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (isSystemCaller()) {
                                return;
                            }
                            Object r = param.getResult();
                            if (!(r instanceof NetworkCapabilities)) {
                                return;
                            }
                            NetworkCapabilities nc = (NetworkCapabilities) r;
                            boolean wifi = nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                            boolean ethernet = nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                            boolean vpn = nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
                            boolean cell = nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                            if (!(wifi || ethernet || vpn) || cell) {
                                return;
                            }
                            try {
                                Object copy = XposedHelpers.newInstance(NetworkCapabilities.class, nc);
                                XposedHelpers.callMethod(copy, "removeTransportType",
                                        NetworkCapabilities.TRANSPORT_WIFI);
                                XposedHelpers.callMethod(copy, "removeTransportType",
                                        NetworkCapabilities.TRANSPORT_ETHERNET);
                                XposedHelpers.callMethod(copy, "removeTransportType",
                                        NetworkCapabilities.TRANSPORT_VPN);
                                XposedHelpers.callMethod(copy, "addTransportType",
                                        NetworkCapabilities.TRANSPORT_CELLULAR);
                                XposedHelpers.callMethod(copy, "removeCapability",
                                        NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
                                param.setResult(copy);
                            } catch (Throwable t) {
                                loge("nc spoof failed: " + t);
                            }
                        }
                    });
                } else if ("isActiveNetworkMetered".equals(m.getName())
                        && m.getReturnType() == boolean.class) {
                    hookMethod(m, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (isSystemCaller()) {
                                return;
                            }
                            param.setResult(true);
                        }
                    });
                } else if (("getActiveNetworkInfo".equals(m.getName())
                        || "getActiveNetworkInfoForUid".equals(m.getName())
                        || "getNetworkInfo".equals(m.getName())
                        || "getNetworkInfoForUid".equals(m.getName()))
                        && m.getReturnType() == android.net.NetworkInfo.class) {
                    hookMethod(m, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (isSystemCaller()) {
                                return;
                            }
                            spoofNetworkInfo(param.getResult());
                        }
                    });
                }
            } catch (Throwable t) {
                loge("connectivity hook err " + m.getName() + ": " + t);
            }
        }
    }

    private static void spoofNetworkInfo(Object ni) {
        if (ni == null) {
            return;
        }
        try {
            XposedHelpers.setIntField(ni, "mNetworkType", 0);
            XposedHelpers.setIntField(ni, "mSubtype", SimProfile.NETWORK_TYPE_LTE);
            XposedHelpers.setObjectField(ni, "mTypeName", "MOBILE");
            XposedHelpers.setObjectField(ni, "mSubtypeName", "LTE");
            Class<?> stateCls = XposedHelpers.findClass("android.net.NetworkInfo$State", null);
            Object stateConnected = Enum.valueOf((Class) stateCls, "CONNECTED");
            XposedHelpers.setObjectField(ni, "mState", stateConnected);
            Class<?> detailCls = XposedHelpers.findClass("android.net.NetworkInfo$DetailedState", null);
            Object detailConnected = Enum.valueOf((Class) detailCls, "CONNECTED");
            XposedHelpers.setObjectField(ni, "mDetailedState", detailConnected);
            XposedHelpers.setBooleanField(ni, "mIsAvailable", true);
        } catch (Throwable t) {
            loge("spoofNetworkInfo failed: " + t);
        }
    }

    // ---------------------------------------------------------------------
    // system_server: 对普通 App 隐藏“开发者选项 / USB 调试已开启”
    // ---------------------------------------------------------------------

    private static volatile boolean sSettingsHooked = false;

    private static void hookSettings(ClassLoader cl) {
        final Class<?> direct = findClassQuiet(cl, "com.android.providers.settings.SettingsProvider");
        if (direct != null) {
            hookSettingsProvider(direct);
            return;
        }
        // SettingsProvider 不在 system_server 的 classloader 里：用 attachInfo 捕获实例再钩 call
        final Class<?> cp = findClassQuiet(cl, "android.content.ContentProvider");
        if (cp == null) {
            loge("ContentProvider not found");
            return;
        }
        for (final Method m : cp.getDeclaredMethods()) {
            if (!m.getName().equals("attachInfo")) {
                continue;
            }
            hookMethod(m, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam p) {
                    final Object provider = p.thisObject;
                    if (provider == null) {
                        return;
                    }
                    if ("com.android.providers.settings.SettingsProvider"
                            .equals(provider.getClass().getName())) {
                        hookSettingsProvider(provider.getClass());
                    }
                }
            });
        }
        logd("waiting to capture SettingsProvider via attachInfo");
    }

    private static void hookSettingsProvider(Class<?> c) {
        if (sSettingsHooked) {
            return;
        }
        sSettingsHooked = true;
        logi("SettingsProvider captured: " + c.getName());
        for (final Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals("call") || m.getReturnType() != Bundle.class) {
                continue;
            }
            Class<?>[] pt = m.getParameterTypes();
            if (pt.length != 3 || pt[0] != String.class || pt[1] != String.class
                    || pt[2] != Bundle.class) {
                continue;
            }
            hookMethod(m, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam p) {
                    if (isSystemCaller()) {
                        return;
                    }
                    try {
                        Object method = p.args[0];
                        Object name = p.args[1];
                        if (!"GET_global".equals(method) && !"GET_secure".equals(method)) {
                            return;
                        }
                        if (!"adb_enabled".equals(name)
                                && !"development_settings_enabled".equals(name)
                                && !"adb_wifi_enabled".equals(name)) {
                            return;
                        }
                        Object r = p.getResult();
                        if (r instanceof Bundle) {
                            ((Bundle) r).putString("value", "0");
                            logi("hid setting " + name + " uid=" + uid());
                        }
                    } catch (Throwable t) {
                        loge("settings spoof failed: " + t);
                    }
                }
            });
        }
    }

    // ---------------------------------------------------------------------
    // Hook helpers
    // ---------------------------------------------------------------------

    /** 系统 / root / phone 自身的调用不伪造，交给原逻辑。 */
    private static boolean isSystemCaller() {
        try {
            final int uid = Binder.getCallingUid();
            return uid == 0 || uid == Process.SYSTEM_UID || uid == Process.PHONE_UID;
        } catch (Throwable t) {
            return false;
        }
    }

    /** 值是运行时读取的（设置页改了、最多几秒生效，无需重启）。 */
    private interface Value {
        Object get();
    }

    private static void hookString(Class<?> c, String name, final Value value) {
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals(name) || m.getReturnType() != String.class) {
                continue;
            }
            final String mn = m.getName();
            hookMethod(m, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (isSystemCaller()) {
                        return;
                    }
                    Object v = value.get();
                    if (v != null) {
                        logd("fire " + mn + " -> " + v);
                        param.setResult(v);
                    }
                }
            });
        }
    }

    private static void hookInt(Class<?> c, String name, final Value value) {
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals(name) || m.getReturnType() != int.class) {
                continue;
            }
            final String mn = m.getName();
            hookMethod(m, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (isSystemCaller()) {
                        return;
                    }
                    Object v = value.get();
                    if (v != null) {
                        logd("fire " + mn + " -> " + v);
                        param.setResult(v);
                    }
                }
            });
        }
    }

    private static void hookBool(Class<?> c, String name, final Value value) {
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals(name) || m.getReturnType() != boolean.class) {
                continue;
            }
            final String mn = m.getName();
            hookMethod(m, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (isSystemCaller()) {
                        return;
                    }
                    Object v = value.get();
                    if (v != null) {
                        logd("fire " + mn + " -> " + v);
                        param.setResult(v);
                    }
                }
            });
        }
    }

    private static int uid() {
        try {
            return Binder.getCallingUid();
        } catch (Throwable t) {
            return -1;
        }
    }

    private static void hookSubscriptionList(Class<?> c, String name, final Class<?> subInfo,
                                             final ClassLoader cl) {
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals(name) || !List.class.isAssignableFrom(m.getReturnType())) {
                continue;
            }
            final String mn = m.getName();
            hookMethod(m, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (isSystemCaller()) {
                        return;
                    }
                    logd("fire " + mn + " -> [fake sub]");
                    Object info = buildSubscriptionInfo(subInfo, ProfileStore.get(cl));
                    param.setResult(info == null
                            ? Collections.emptyList()
                            : new ArrayList<>(Collections.singletonList(info)));
                }
            });
        }
    }

    private static void hookSubscriptionSingle(Class<?> c, String name, final Class<?> subInfo,
                                               final ClassLoader cl) {
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals(name) || m.getReturnType() != subInfo) {
                continue;
            }
            final String mn = m.getName();
            hookMethod(m, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (isSystemCaller()) {
                        return;
                    }
                    logd("fire " + mn + " -> [fake sub]");
                    Object info = buildSubscriptionInfo(subInfo, ProfileStore.get(cl));
                    if (info != null) {
                        param.setResult(info);
                    }
                }
            });
        }
    }

    private static void hookMethod(Method m, XC_MethodHook cb) {
        try {
            XposedBridge.hookMethod(m, cb);
            logd("hooked " + m.getDeclaringClass().getSimpleName() + "#" + m.getName());
        } catch (Throwable t) {
            loge("hook failed " + m.getName() + ": " + t);
        }
    }

    private static Class<?> findClass(ClassLoader cl, String name) {
        try {
            return XposedHelpers.findClass(name, cl);
        } catch (Throwable t) {
            loge("class not found: " + name);
            return null;
        }
    }

    // ---------------------------------------------------------------------
    // SubscriptionInfo factory (best effort, field names differ by API level)
    // ---------------------------------------------------------------------

    private static Object buildSubscriptionInfo(Class<?> siClass, ProfileStore.Snapshot s) {
        try {
            Constructor<?> chosen = null;
            for (Constructor<?> c : siClass.getDeclaredConstructors()) {
                Class<?>[] p = c.getParameterTypes();
                if (p.length >= 16
                        && p[0] == int.class
                        && p[1] == String.class
                        && p[2] == int.class
                        && CharSequence.class.isAssignableFrom(p[3])
                        && CharSequence.class.isAssignableFrom(p[4])
                        && p[11] == String.class
                        && p[12] == String.class) {
                    if (chosen == null || p.length < chosen.getParameterTypes().length) {
                        chosen = c;
                    }
                }
            }
            if (chosen == null) {
                loge("no SubscriptionInfo constructor matched");
                return null;
            }
            Class<?>[] p = chosen.getParameterTypes();
            Object[] a = new Object[p.length];
            for (int i = 0; i < p.length; i++) {
                a[i] = defaultFor(p[i]);
            }
            a[0] = s.subscriptionId;
            a[1] = s.iccid;
            a[2] = 0;
            a[3] = s.operatorName;
            a[4] = s.operatorName;
            a[5] = 1;
            a[6] = 0;
            a[7] = s.line1;
            a[8] = 0;
            a[9] = null;
            a[10] = String.valueOf(s.mcc);
            a[11] = String.valueOf(s.mnc);
            a[12] = s.countryIso;
            if (p.length > 13 && p[13] == boolean.class) {
                a[13] = false;
            }
            if (p.length > 14 && p[14].isArray()) {
                a[14] = null;
            }
            if (p.length > 15 && p[15] == String.class) {
                a[15] = "";
            }
            chosen.setAccessible(true);
            Object info = chosen.newInstance(a);
            logd("built SubscriptionInfo ctor len=" + p.length);
            return info;
        } catch (Throwable t) {
            loge("buildSubscriptionInfo failed: " + t);
        }
        return null;
    }

    private static Object defaultFor(Class<?> t) {
        if (t == int.class) return 0;
        if (t == long.class) return 0L;
        if (t == boolean.class) return false;
        if (t == byte.class) return (byte) 0;
        if (t == short.class) return (short) 0;
        if (t == char.class) return (char) 0;
        if (t == float.class) return 0f;
        if (t == double.class) return 0d;
        if (t.isArray()) return Array.newInstance(t.getComponentType(), 0);
        if (t == String.class) return "";
        if (CharSequence.class.isAssignableFrom(t)) return "";
        if (List.class.isAssignableFrom(t)) return new ArrayList<>();
        return null;
    }

    private static void logi(String msg) {
        SimLog.i(msg);
    }

    private static void logd(String msg) {
        SimLog.d(msg);
    }

    private static void loge(String msg) {
        SimLog.e(msg);
    }
}
