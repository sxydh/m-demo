package com.rootdemo.simspoofer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;

import de.robv.android.xposed.XposedHelpers;

/**
 * 运行时档案存取：
 *  - 设置页把值写进模块 App 的 SharedPreferences；
 *  - 通过 ContentProvider 暴露给 phone 进程里的 Xposed 模块；
 *  - 模块每次调用时读取（3 秒缓存），改完无需重启。
 *
 * 取不到配置时回退到 SimProfile 里的默认值。
 */
public final class ProfileStore {

    public static final String AUTHORITY = "com.rootdemo.simspoofer.config";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/profile");
    public static final String PREFS = "sim_profile";

    /** provider 返回的列顺序（与 query 的 projection 对应）。 */
    public static final String[] COLUMNS = {
            "line1", "iccid", "imsi",
            "sim_operator", "network_operator", "operator_name", "network_operator_name",
            "country_iso", "mcc", "mnc", "subscription_id", "sim_state", "network_type", "has_icc",
            "debug"
    };

    public static final class Snapshot {
        public String line1 = SimProfile.LINE1_NUMBER;
        public String iccid = SimProfile.ICCID;
        public String imsi = SimProfile.IMSI;
        public String simOperator = SimProfile.SIM_OPERATOR;
        public String networkOperator = SimProfile.NETWORK_OPERATOR;
        public String operatorName = SimProfile.SIM_OPERATOR_NAME;
        public String networkOperatorName = SimProfile.NETWORK_OPERATOR_NAME;
        public String countryIso = SimProfile.COUNTRY_ISO;
        public int mcc = SimProfile.MCC;
        public int mnc = SimProfile.MNC;
        public int subscriptionId = SimProfile.SUBSCRIPTION_ID;
        public int simState = SimProfile.SIM_STATE_READY;
        public int networkType = SimProfile.NETWORK_TYPE_LTE;
        public boolean hasIcc = true;
        public boolean debug = false;
    }

    private static volatile Snapshot sCache;
    private static volatile long sCacheAt;
    private static final long TTL_MS = 3000L;

    private ProfileStore() {
    }

    public static Snapshot get(ClassLoader cl) {
        final long now = SystemClock.uptimeMillis();
        final Snapshot cached = sCache;
        if (cached != null && now - sCacheAt < TTL_MS) {
            return cached;
        }
        final Snapshot loaded = load(cl);
        if (loaded != null) {
            sCache = loaded;
            sCacheAt = now;
            SimLog.setLevel(loaded.debug ? SimLog.DEBUG : SimLog.INFO);
            return loaded;
        }
        return cached != null ? cached : new Snapshot();
    }

    public static void invalidate() {
        sCache = null;
    }

    private static Snapshot load(ClassLoader cl) {
        try {
            final Context ctx = currentContext(cl);
            if (ctx == null) {
                return null;
            }
            final Cursor cur = ctx.getContentResolver().query(URI, COLUMNS, null, null, null);
            if (cur == null) {
                return null;
            }
            try {
                if (!cur.moveToFirst()) {
                    return null;
                }
                final Snapshot s = new Snapshot();
                s.line1 = str(cur, "line1", s.line1);
                s.iccid = str(cur, "iccid", s.iccid);
                s.imsi = str(cur, "imsi", s.imsi);
                s.simOperator = str(cur, "sim_operator", s.simOperator);
                s.networkOperator = str(cur, "network_operator", s.networkOperator);
                s.operatorName = str(cur, "operator_name", s.operatorName);
                s.networkOperatorName = str(cur, "network_operator_name", s.networkOperatorName);
                s.countryIso = str(cur, "country_iso", s.countryIso);
                s.mcc = integer(cur, "mcc", s.mcc);
                s.mnc = integer(cur, "mnc", s.mnc);
                s.subscriptionId = integer(cur, "subscription_id", s.subscriptionId);
                s.simState = integer(cur, "sim_state", s.simState);
                s.networkType = integer(cur, "network_type", s.networkType);
                s.hasIcc = integer(cur, "has_icc", 1) != 0;
                s.debug = integer(cur, "debug", 0) != 0;
                return s;
            } finally {
                cur.close();
            }
        } catch (Throwable t) {
            return null;
        }
    }

    private static Context currentContext(ClassLoader cl) {
        try {
            final Class<?> at = XposedHelpers.findClass("android.app.ActivityThread", cl);
            final Object app = XposedHelpers.callStaticMethod(at, "currentApplication");
            if (app instanceof Context) {
                return (Context) app;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static String str(Cursor c, String col, String def) {
        final int i = c.getColumnIndex(col);
        if (i < 0) {
            return def;
        }
        final String v = c.getString(i);
        return v == null ? def : v;
    }

    private static int integer(Cursor c, String col, int def) {
        final int i = c.getColumnIndex(col);
        if (i < 0) {
            return def;
        }
        return c.getInt(i);
    }
}
