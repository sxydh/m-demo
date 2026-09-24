"""刷新 Token
POST /apigw/v1/refreshToken

先跑 verify_otp（3032）和 pin_login（3033）拿到 refreshToken，再运行本接口。
本接口为明文直连（非 userData 加密通道）。
"""

import json

import requests

from common.cipher import decrypt
from common.client import PROXIES, bearer
from common.config import (
    BASE_URL,
    CHANNEL_ID,
    OUTER_PASSWORD,
    OUTPUT_DIR,
    TIMEOUT,
)
from common.log import log

with open(OUTPUT_DIR + "/verify_otp.json", encoding="utf-8") as f:
    verify_resp = json.load(f)
verify = json.loads(decrypt(verify_resp["userData"], OUTER_PASSWORD))
secret_key = verify["data"]["secretKey"]

with open(OUTPUT_DIR + "/pin_login.json", encoding="utf-8") as f:
    pin_resp = json.load(f)
pin = json.loads(decrypt(pin_resp["userData"], secret_key))

url = BASE_URL.rstrip("/") + "/apigw/v1/refreshToken"
headers = {
    "Authorization": bearer(pin["data"]["refreshToken"]),
    "CHANNEL_ID": CHANNEL_ID,
    "Content-Type": "application/json; charset=utf-8",
    "User-Agent": "okhttp/4.9.3",
}

log("[接口] 刷新 Token")
log("POST", url)
resp = requests.post(url, data="{}", headers=headers, timeout=TIMEOUT, proxies=PROXIES)

path = OUTPUT_DIR + "/refresh_token.json"
with open(path, "w", encoding="utf-8") as f:
    f.write(resp.text)
log("saved", path, "HTTP", resp.status_code)

assert resp.status_code == 200, "HTTP %d: %s" % (resp.status_code, resp.text)
log(json.dumps(resp.json(), ensure_ascii=False, indent=2))
