"""发送 OTP
POST /apigw/v1/userService
"""

import json
from datetime import datetime

from common.cipher import decrypt
from common.client import bearer, post
from common.config import (
    BASE_URL_TXN,
    CLIENT_LANGUAGE,
    CLIENT_VERSION,
    DEVICE_ID,
    DEVICE_INFO,
    OUTER_PASSWORD,
    OUTPUT_DIR,
    PHONE,
    ROCKET_SESSION_ID,
)

with open(OUTPUT_DIR + "/login.json", encoding="utf-8") as f:
    login_resp = json.load(f)
login = json.loads(decrypt(login_resp["userData"], OUTER_PASSWORD))

with open(OUTPUT_DIR + "/reg_init.json", encoding="utf-8") as f:
    reg_resp = json.load(f)
reg = json.loads(decrypt(reg_resp["userData"], OUTER_PASSWORD))

now = datetime.now()
payload = {
    "apiId": 3057,
    "deviceId": "ANDROID-%s" % DEVICE_ID,
    "deviceInfo": DEVICE_INFO,
    "clientVersion": CLIENT_VERSION,
    "refId": "get-OTP-%s%03d" % (now.strftime("%Y%m%d%H%M%S"), now.microsecond // 1000),
    "sessionId": login["data"]["sessionId"],
    "clientLanguage": CLIENT_LANGUAGE,
    "data": {
        "appType": "C",
        "initiatorId": PHONE,
        "rocketSessionId": ROCKET_SESSION_ID,
        "userId": "",
        "version": CLIENT_VERSION,
        "otpRefNo": reg["data"]["otpRefNo"],
    },
}

url = BASE_URL_TXN.rstrip("/") + "/apigw/v1/userService"
resp = post(url, payload, bearer(login["data"]["accessToken"]), "send_otp.json")

assert resp.status_code == 200, "HTTP %d: %s" % (resp.status_code, resp.text)
data = json.loads(decrypt(resp.json()["userData"], OUTER_PASSWORD))
assert data.get("status") == "SUCCESS", "status=%r body=%s" % (data.get("status"), data)
print(json.dumps(data, ensure_ascii=False, indent=2))
