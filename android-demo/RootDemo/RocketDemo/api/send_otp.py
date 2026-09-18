"""发送 OTP
POST /apigw/v1/userService
"""

import json
from datetime import datetime

import requests

from cipher import decrypt, encrypt
from config import (
    BASE_URL_TXN,
    CHANNEL_ID,
    CLIENT_LANGUAGE,
    CLIENT_VERSION,
    DEVICE_ID,
    DEVICE_INFO,
    OUTER_PASSWORD,
    OUTPUT_DIR,
    PHONE,
    ROCKET_SESSION_ID,
    TIMEOUT,
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

plain = json.dumps(payload, ensure_ascii=False, separators=(",", ":"))
body = {"userData": encrypt(plain, OUTER_PASSWORD)}
headers = {
    "Authorization": "Bearer " + login["data"]["accessToken"],
    "CHANNEL_ID": CHANNEL_ID,
    "Content-Type": "application/json",
    "User-Agent": "okhttp/4.9.3",
}

url = BASE_URL_TXN.rstrip("/") + "/apigw/v1/userService"
print("POST", url)
resp = requests.post(url, json=body, headers=headers, timeout=TIMEOUT)

path = OUTPUT_DIR + "/send_otp.json"
with open(path, "w", encoding="utf-8") as f:
    f.write(resp.text)
print("saved", path, "HTTP", resp.status_code)

assert resp.status_code == 200, "HTTP %d: %s" % (resp.status_code, resp.text)
data = json.loads(decrypt(resp.json()["userData"], OUTER_PASSWORD))
assert data.get("status") == "SUCCESS", "status=%r body=%s" % (data.get("status"), data)
print(json.dumps(data, ensure_ascii=False, indent=2))
