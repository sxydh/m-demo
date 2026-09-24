"""PIN 登录（Inner 通道）
POST /apigw/v1/login

先跑 verify_otp（3032）拿到 secretKey，再运行本接口。
用法: python -m api.pin_login   （交互式输入 PIN）
"""

import json
from datetime import datetime

from common.cipher import decrypt, encrypt
from common.client import inner_auth, post
from common.config import (
    BASE_URL_TXN,
    CLIENT_LANGUAGE,
    CLIENT_VERSION,
    DEVICE_ID,
    DEVICE_INFO,
    GATEWAY_SESSION_ID,
    OUTER_PASSWORD,
    OUTPUT_DIR,
    PHONE,
)
from common.log import log

with open(OUTPUT_DIR + "/verify_otp.json", encoding="utf-8") as f:
    verify_resp = json.load(f)
verify = json.loads(decrypt(verify_resp["userData"], OUTER_PASSWORD))
secret_key = verify["data"]["secretKey"]

pin = input("PIN: ")

now = datetime.now()
payload = {
    "apiId": 3033,
    "deviceId": "ANDROID-%s" % DEVICE_ID,
    "deviceInfo": DEVICE_INFO,
    "clientVersion": CLIENT_VERSION,
    "refId": "login-%s%03d" % (now.strftime("%Y%m%d%H%M%S"), now.microsecond // 1000),
    "sessionId": GATEWAY_SESSION_ID,
    "clientLanguage": CLIENT_LANGUAGE,
    "data": {
        "loginMode": "PIN",
        "userId": PHONE,
        "password": encrypt(pin, OUTER_PASSWORD),
    },
}

url = BASE_URL_TXN.rstrip("/") + "/apigw/v1/login"
resp = post(
    url,
    payload,
    inner_auth(),
    "pin_login.json",
    "PIN 登录",
    extra_headers={"X-USER": encrypt(PHONE, OUTER_PASSWORD)},
    password=secret_key,
)

assert resp.status_code == 200, "HTTP %d: %s" % (resp.status_code, resp.text)
data = json.loads(decrypt(resp.json()["userData"], secret_key))
log(json.dumps(data, ensure_ascii=False, indent=2))
