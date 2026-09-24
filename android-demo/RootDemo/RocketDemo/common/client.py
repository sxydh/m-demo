"""统一请求发送：加密、固定头部、走 PC 代理，返回原始响应"""

import base64
import json

import requests

from common.cipher import encrypt
from common.config import (
    BASIC_CREDENTIALS,
    CHANNEL_ID,
    INNER_CREDENTIALS,
    OUTER_PASSWORD,
    OUTPUT_DIR,
    PROXY,
    TIMEOUT,
)
from common.log import log

PROXIES = {"http": PROXY, "https": PROXY} if PROXY else None


def basic_auth():
    return "Basic " + base64.b64encode(BASIC_CREDENTIALS.encode("utf-8")).decode("ascii")


def inner_auth():
    return "Basic " + base64.b64encode(INNER_CREDENTIALS.encode("utf-8")).decode("ascii")


def bearer(token):
    return "Bearer " + token


def post(url, payload, auth, save_name, desc="", extra_headers=None, password=OUTER_PASSWORD):
    plain = json.dumps(payload, ensure_ascii=False, separators=(",", ":"))
    body = {"userData": encrypt(plain, password)}
    headers = {
        "Authorization": auth,
        "CHANNEL_ID": CHANNEL_ID,
        "Content-Type": "application/json",
        "User-Agent": "okhttp/4.9.3",
    }
    if extra_headers:
        headers.update(extra_headers)
    log("[接口] %s" % desc if desc else "[接口] (未命名)")
    log("POST", url)
    resp = requests.post(url, json=body, headers=headers, timeout=TIMEOUT, proxies=PROXIES)

    path = OUTPUT_DIR + "/" + save_name
    with open(path, "w", encoding="utf-8") as f:
        f.write(resp.text)
    log("saved", path, "HTTP", resp.status_code)
    return resp
