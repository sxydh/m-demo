"""刷新 Token
POST /apigw/v1/refreshToken

自动取最新 refreshToken（refresh_token.json 优先于 pin_login.json）。
本接口为明文直连（非 userData 加密通道），响应为明文 JSON。
"""

import json

import requests

from common.client import PROXIES, bearer
from common.config import (
    BASE_URL,
    CHANNEL_ID,
    OUTPUT_DIR,
    TIMEOUT,
)
from common.log import log
from common.session import refresh_token

url = BASE_URL.rstrip("/") + "/apigw/v1/refreshToken"
headers = {
    "Authorization": bearer(refresh_token()),
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
