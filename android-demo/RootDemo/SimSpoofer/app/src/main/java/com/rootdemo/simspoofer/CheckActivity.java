package com.rootdemo.simspoofer;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

/**
 * 自检页：以普通 App 身份读取 telephony，验证 phone 进程里的伪造是否生效。
 * 启动：adb shell am start -n com.rootdemo.simspoofer/.CheckActivity
 */
public class CheckActivity extends Activity {

    private static final String TAG = "SimSpooferCheck";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final StringBuilder sb = new StringBuilder();
        try {
            final TelephonyManager tm = getSystemService(TelephonyManager.class);
            sb.append("simState      = ").append(tm.getSimState()).append('\n');
            sb.append("hasIccCard    = ").append(tm.hasIccCard()).append('\n');
            sb.append("simOperator   = ").append(tm.getSimOperator()).append('\n');
            sb.append("simOpName     = ").append(tm.getSimOperatorName()).append('\n');
            sb.append("simSerial(ICCID) = ").append(tm.getSimSerialNumber()).append('\n');
            sb.append("subscriberId(IMSI) = ").append(tm.getSubscriberId()).append('\n');
            sb.append("line1Number   = ").append(tm.getLine1Number()).append('\n');
            sb.append("phoneCount    = ").append(tm.getPhoneCount()).append('\n');

            sb.append("simOpNumeric  = ").append(tm.getSimOperator()).append('\n');
            sb.append("netOperator   = ").append(tm.getNetworkOperator()).append('\n');
            sb.append("activeModems  = ").append(tm.getActiveModemCount()).append('\n');
            sb.append("supportedModems = ").append(tm.getSupportedModemCount()).append('\n');

            final SubscriptionManager sm = getSystemService(SubscriptionManager.class);
            final List<SubscriptionInfo> list = sm.getActiveSubscriptionInfoList();
            sb.append("subList       = ").append(list == null ? "null" : list.size()).append('\n');
            if (list != null && !list.isEmpty()) {
                final SubscriptionInfo info = list.get(0);
                sb.append("  id=").append(info.getSubscriptionId())
                        .append(" iccid=").append(info.getIccId())
                        .append(" carrier=").append(info.getCarrierName())
                        .append('\n');
                sb.append("  mcc=").append(info.getMcc())
                        .append(" mnc=").append(info.getMnc())
                        .append(" mccString=").append(info.getMccString())
                        .append(" mncString=").append(info.getMncString())
                        .append(" country=").append(info.getCountryIso())
                        .append(" slot=").append(info.getSimSlotIndex())
                        .append(" number=").append(info.getNumber())
                        .append('\n');
                for (java.lang.reflect.Field f : info.getClass().getDeclaredFields()) {
                    try {
                        f.setAccessible(true);
                        sb.append("  field ").append(f.getName()).append('=')
                                .append(f.get(info)).append('\n');
                    } catch (Throwable ignored) {
                    }
                }
            }
            sb.append("defaultSubId  = ").append(SubscriptionManager.getDefaultSubscriptionId()).append('\n');
            sb.append("defaultDataSubId  = ").append(SubscriptionManager.getDefaultDataSubscriptionId()).append('\n');
            sb.append("defaultVoiceSubId = ").append(SubscriptionManager.getDefaultVoiceSubscriptionId()).append('\n');
            sb.append("defaultSmsSubId   = ").append(SubscriptionManager.getDefaultSmsSubscriptionId()).append('\n');
            sb.append("activeSubCount    = ").append(sm.getActiveSubscriptionInfoCount()).append('\n');
            sb.append("--- SubscriptionInfo declared fields ---\n");
            for (java.lang.reflect.Field f : SubscriptionInfo.class.getDeclaredFields()) {
                sb.append("  ").append(f.getType().getSimpleName()).append(' ')
                        .append(f.getName()).append('\n');
            }
        } catch (Throwable t) {
            sb.append("ERR: ").append(t).append('\n');
        }
        Log.i(TAG, "\n" + sb);
        final TextView tv = new TextView(this);
        tv.setText(sb.toString());
        tv.setTextSize(14f);
        setContentView(tv);
    }
}
