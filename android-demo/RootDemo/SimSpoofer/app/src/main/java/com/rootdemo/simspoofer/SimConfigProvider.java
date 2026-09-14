package com.rootdemo.simspoofer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * 把模块 App 里的 SIM 档案（SharedPreferences）暴露给 phone 进程的 Xposed 模块读取。
 * 导出、无权限限制（仅本 demo 用）。
 */
public class SimConfigProvider extends ContentProvider {

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(ProfileStore.PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SharedPreferences p = prefs(getContext());
        final MatrixCursor cur = new MatrixCursor(ProfileStore.COLUMNS);
        cur.addRow(new Object[]{
                p.getString("line1", SimProfile.LINE1_NUMBER),
                p.getString("iccid", SimProfile.ICCID),
                p.getString("imsi", SimProfile.IMSI),
                p.getString("sim_operator", SimProfile.SIM_OPERATOR),
                p.getString("network_operator", SimProfile.NETWORK_OPERATOR),
                p.getString("operator_name", SimProfile.SIM_OPERATOR_NAME),
                p.getString("network_operator_name", SimProfile.NETWORK_OPERATOR_NAME),
                p.getString("country_iso", SimProfile.COUNTRY_ISO),
                p.getInt("mcc", SimProfile.MCC),
                p.getInt("mnc", SimProfile.MNC),
                p.getInt("subscription_id", SimProfile.SUBSCRIPTION_ID),
                p.getInt("sim_state", SimProfile.SIM_STATE_READY),
                p.getInt("network_type", SimProfile.NETWORK_TYPE_LTE),
                p.getBoolean("has_icc", true) ? 1 : 0
        });
        return cur;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd." + ProfileStore.AUTHORITY;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
