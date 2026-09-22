import json
import os
from pathlib import Path

GW = os.environ.get("TAQU_GW", "https://gw-cn.jiaoliuqu.com")
PHONE = os.environ.get("TAQU_PHONE", "16689602694")
DEVICE_ID = os.environ.get("TAQU_DEVICE_ID", "M3e118bfb1d26a280a17d28a1eead932b")
SHUMEI_DEVICE_ID = os.environ.get(
    "TAQU_SHUMEI_DEVICE_ID",
    "20260922155906270431b6b7f61a4f2554d74e03625a640077d8da515e2b34",
)
TIMEOUT = 20

ROOT = Path(__file__).resolve().parents[1]
UNIDBG_DIR = ROOT / "_Temp" / "unidbg-call"
SO_PATH = UNIDBG_DIR / "libnative-xbp.so"
CP_FILE = UNIDBG_DIR / "cp.txt"
OUTPUT_DIR = ROOT / "_Temp"
os.makedirs(OUTPUT_DIR, exist_ok=True)

LOGIN_EXTRA_JSON = json.dumps(
    {
        "mobile_brand": "google",
        "mobile_model": "Pixel 7 Pro",
        "mobile_sys_version": "16",
        "network_service": "",
        "network_type": "WWAN",
    },
    ensure_ascii=False,
    separators=(",", ":"),
)

_COMMON_HEADERS = {
    "host": "gw-cn.jiaoliuqu.com",
    "ch": "yingyongbao",
    "user-agent": "8695",
    "cl": "1",
    "lang": "zh_cn",
    "ap": "1",
    "av": "8695",
    "appid": "1001",
    "to": DEVICE_ID,
    "pl": "android",
    "accept-encoding": "gzip",
}

FORM_HEADERS = {
    **_COMMON_HEADERS,
    "ac": "NON",
    "lo": "",
    "sx": "",
    "ci": "0",
    "la": "",
    "content-type": "application/x-www-form-urlencoded",
}

GET_HEADERS = {
    **_COMMON_HEADERS,
    "ac": "WWAN",
    "lo": "104.07833153",
    "sx": "1",
    "ci": "2590",
    "la": "30.52765161",
    "abep": (
        "1046_2803,1053_2818,1213_3161,986_2670,1083_2880,1168_3065,1076_2866,"
        "1102_2923,1099_2917,712_2009,1189_3108,1177_3083,1146_3019,1161_3051"
    ),
}
