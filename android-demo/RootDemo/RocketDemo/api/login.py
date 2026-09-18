"""手机号登录
POST /apigw/v1/login
"""

import base64
import json
from datetime import datetime

import requests

from cipher import decrypt, encrypt
from config import (
    BASE_URL,
    BASIC_CREDENTIALS,
    CHANNEL_ID,
    CLIENT_LANGUAGE,
    CLIENT_VERSION,
    DEVICE_ID,
    DEVICE_INFO,
    GATEWAY_SESSION_ID,
    OUTER_PASSWORD,
    OUTPUT_DIR,
    PHONE,
    PRE_SESSION_ID,
    TIMEOUT,
)

now = datetime.now()
payload = {
    "apiId": 102,
    "deviceId": "ANDROID-%s" % DEVICE_ID,
    "deviceInfo": DEVICE_INFO,
    "clientVersion": CLIENT_VERSION,
    "refId": "login-%s%03d" % (now.strftime("%Y%m%d%H%M%S"), now.microsecond // 1000),
    "sessionId": GATEWAY_SESSION_ID,
    "clientLanguage": CLIENT_LANGUAGE,
    "data": {
        "appType": "C",
        "rocketSessionId": PRE_SESSION_ID,
        "userId": PHONE,
        "version": CLIENT_VERSION,
    },
}

plain = json.dumps(payload, ensure_ascii=False, separators=(",", ":"))
body = {"userData": encrypt(plain, OUTER_PASSWORD)}
headers = {
    "Authorization": "Basic " + base64.b64encode(BASIC_CREDENTIALS.encode("utf-8")).decode("ascii"),
    "CHANNEL_ID": CHANNEL_ID,
    "Content-Type": "application/json",
    "User-Agent": "okhttp/4.9.3",
}

url = BASE_URL.rstrip("/") + "/apigw/v1/login"
print("POST", url)
resp = requests.post(url, json=body, headers=headers, timeout=TIMEOUT)

path = OUTPUT_DIR + "/login.json"
with open(path, "w", encoding="utf-8") as f:
    f.write(resp.text)
print("saved", path, "HTTP", resp.status_code)

assert resp.status_code == 200, "HTTP %d: %s" % (resp.status_code, resp.text)
data = json.loads(decrypt(resp.json()["userData"], OUTER_PASSWORD))
assert data.get("status") == "SUCCESS", "status=%r body=%s" % (data.get("status"), data)
print(json.dumps(data, ensure_ascii=False, indent=2))

for name in ("accessToken", "refreshToken"):
    part = data["data"][name].split(".")[1]
    part += "=" * (-len(part) % 4)
    print(name, json.dumps(json.loads(base64.urlsafe_b64decode(part)), ensure_ascii=False))
