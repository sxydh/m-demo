"""手机号登录
POST /apigw/v1/login
"""

import base64
import json
from datetime import datetime

from common.cipher import decrypt
from common.client import basic_auth, post
from common.config import (
    BASE_URL,
    CLIENT_LANGUAGE,
    CLIENT_VERSION,
    DEVICE_ID,
    DEVICE_INFO,
    GATEWAY_SESSION_ID,
    OUTER_PASSWORD,
    PHONE,
    PRE_SESSION_ID,
)
from common.log import log

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

url = BASE_URL.rstrip("/") + "/apigw/v1/login"
resp = post(url, payload, basic_auth(), "login.json", "手机号登录")

assert resp.status_code == 200, "HTTP %d: %s" % (resp.status_code, resp.text)
data = json.loads(decrypt(resp.json()["userData"], OUTER_PASSWORD))
assert data.get("status") == "SUCCESS", "status=%r body=%s" % (data.get("status"), data)
log(json.dumps(data, ensure_ascii=False, indent=2))

for name in ("accessToken", "refreshToken"):
    part = data["data"][name].split(".")[1]
    part += "=" * (-len(part) % 4)
    log(name, json.dumps(json.loads(base64.urlsafe_b64decode(part)), ensure_ascii=False))
