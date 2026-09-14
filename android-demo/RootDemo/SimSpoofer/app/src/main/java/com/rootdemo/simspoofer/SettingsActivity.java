package com.rootdemo.simspoofer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 设置页：修改伪造的 SIM 档案。顶部是保存/恢复按钮，改完保存即写入 SharedPreferences，
 * phone 进程里的模块最多 3 秒后读到新值（无需重启）。
 */
public class SettingsActivity extends Activity {

    private final String[] keys = {
            "line1", "iccid", "imsi", "sim_operator", "network_operator",
            "operator_name", "network_operator_name", "country_iso",
            "mcc", "mnc", "subscription_id", "sim_state", "network_type"
    };
    private final String[] labels = {
            "手机号 (line1)", "ICCID", "IMSI", "SIM 运营商 (MCCMNC)", "网络运营商 (MCCMNC)",
            "SIM 运营商名", "网络运营商名", "国家码 (country iso)",
            "MCC", "MNC", "订阅 ID", "SIM 状态 (5=READY)", "网络类型 (13=LTE)"
    };
    private final String[] defaults;

    public SettingsActivity() {
        defaults = new String[]{
                SimProfile.LINE1_NUMBER, SimProfile.ICCID, SimProfile.IMSI,
                SimProfile.SIM_OPERATOR, SimProfile.NETWORK_OPERATOR,
                SimProfile.SIM_OPERATOR_NAME, SimProfile.NETWORK_OPERATOR_NAME,
                SimProfile.COUNTRY_ISO, String.valueOf(SimProfile.MCC),
                String.valueOf(SimProfile.MNC), String.valueOf(SimProfile.SUBSCRIPTION_ID),
                String.valueOf(SimProfile.SIM_STATE_READY), String.valueOf(SimProfile.NETWORK_TYPE_LTE)
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences p = getSharedPreferences(ProfileStore.PREFS, Context.MODE_PRIVATE);
        final int pad = (int) (getResources().getDisplayMetrics().density * 16);

        int statusBar = 0;
        final int sbId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (sbId > 0) {
            statusBar = getResources().getDimensionPixelSize(sbId);
        }

        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, statusBar + pad, pad, pad);

        final EditText[] inputs = new EditText[keys.length];
        final CheckBox hasIcc = new CheckBox(this);
        hasIcc.setText("hasIccCard = true");
        hasIcc.setChecked(p.getBoolean("has_icc", true));

        final CheckBox debugLog = new CheckBox(this);
        debugLog.setText("调试日志（逐调用打印，默认关）");
        debugLog.setChecked(p.getBoolean("debug", false));

        final Button save = new Button(this);
        save.setText("保存（改完最多 3 秒生效，无需重启）");
        final Button reset = new Button(this);
        reset.setText("恢复默认");

        // 按钮放最上面，避免滚到底
        root.addView(save);
        root.addView(reset);

        for (int i = 0; i < keys.length; i++) {
            final TextView tv = new TextView(this);
            tv.setText(labels[i]);
            root.addView(tv);
            final EditText et = new EditText(this);
            et.setInputType(InputType.TYPE_CLASS_TEXT);
            et.setText(readValue(p, keys[i], defaults[i]));
            root.addView(et);
            inputs[i] = et;
        }
        root.addView(hasIcc);
        root.addView(debugLog);

        save.setOnClickListener(v -> {
            final SharedPreferences.Editor e = p.edit();
            for (int i = 0; i < keys.length; i++) {
                final String val = inputs[i].getText().toString().trim();
                if (isIntKey(keys[i])) {
                    e.putInt(keys[i], parseInt(val, parseInt(defaults[i], 0)));
                } else {
                    e.putString(keys[i], val);
                }
            }
            e.putBoolean("has_icc", hasIcc.isChecked());
            e.putBoolean("debug", debugLog.isChecked());
            e.apply();
            ProfileStore.invalidate();
            Toast.makeText(this, "已保存（phone 进程最多 3 秒后生效）", Toast.LENGTH_LONG).show();
        });

        reset.setOnClickListener(v -> {
            final SharedPreferences.Editor e = p.edit();
            for (int i = 0; i < keys.length; i++) {
                if (isIntKey(keys[i])) {
                    e.putInt(keys[i], parseInt(defaults[i], 0));
                } else {
                    e.putString(keys[i], defaults[i]);
                }
                inputs[i].setText(defaults[i]);
            }
            e.putBoolean("has_icc", true);
            hasIcc.setChecked(true);
            e.putBoolean("debug", false);
            debugLog.setChecked(false);
            e.apply();
            ProfileStore.invalidate();
        });

        final ScrollView scroll = new ScrollView(this);
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
    }

    private static String readValue(SharedPreferences p, String key, String def) {
        if (isIntKey(key)) {
            return String.valueOf(p.getInt(key, parseInt(def, 0)));
        }
        return p.getString(key, def);
    }

    private static boolean isIntKey(String k) {
        return k.equals("mcc") || k.equals("mnc") || k.equals("subscription_id")
                || k.equals("sim_state") || k.equals("network_type");
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Throwable t) {
            return def;
        }
    }
}
