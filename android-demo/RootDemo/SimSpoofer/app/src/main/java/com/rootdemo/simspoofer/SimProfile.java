package com.rootdemo.simspoofer;

/**
 * 伪造的 SIM 档案。改这里即可，无需改 hook 逻辑。
 * 注意：ICCID 建议 19~20 位；IMSI 15 位且前 5 位与 MCC/MNC 对应；手机号带国家码。
 */
public final class SimProfile {

    private SimProfile() {
    }

    /** 日志级别见 {@link SimLog}；默认 INFO，设置页可开 DEBUG。 */

    /** MCC+MNC，印度 Airtel 40410。 */
    public static final String SIM_OPERATOR = "40410";
    public static final String NETWORK_OPERATOR = "40410";
    public static final String SIM_OPERATOR_NAME = "Airtel";
    public static final String NETWORK_OPERATOR_NAME = "Airtel";
    public static final String COUNTRY_ISO = "in";
    public static final int MCC = 404;
    public static final int MNC = 10;

    /** ICCID：20 位，89 + 91(印度) + 30(Airtel) + 主体 + Luhn 校验位。 */
    public static final String ICCID = "89913012345678901235";
    /** IMSI：15 位（MCC 404 + MNC 10 + 10 位 MSIN）。 */
    public static final String IMSI = "404109876543210";
    /** 手机号（MSISDN）：印度 10 位，+91。 */
    public static final String LINE1_NUMBER = "+919876543210";

    /** TelephonyManager.SIM_STATE_READY == 5。 */
    public static final int SIM_STATE_READY = 5;
    /** TelephonyManager.NETWORK_TYPE_LTE == 13。 */
    public static final int NETWORK_TYPE_LTE = 13;
    /** SubscriptionManager 默认订阅 id。 */
    public static final int SUBSCRIPTION_ID = 1;
}
