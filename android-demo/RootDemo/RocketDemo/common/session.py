"""会话凭据解析：按产物文件修改时间自动选取最新的 token

- refresh_token.json（明文）比 pin_login.json 新 -> 用刷新后的 token
- 否则用 pin_login.json 的 token
"""

import json
import os

from common.cipher import decrypt
from common.config import OUTER_PASSWORD, OUTPUT_DIR


def _path(name):
    return OUTPUT_DIR + "/" + name


def _mtime(name):
    path = _path(name)
    return os.path.getmtime(path) if os.path.exists(path) else None


def _read(name):
    with open(_path(name), encoding="utf-8") as f:
        return json.load(f)


def security_code():
    resp = _read("verify_otp.json")
    return json.loads(decrypt(resp["userData"], OUTER_PASSWORD))["data"]["secretKey"]


def pin_data():
    resp = _read("pin_login.json")
    return json.loads(decrypt(resp["userData"], security_code()))["data"]


def refresh_data():
    resp = _read("refresh_token.json")
    return resp["data"]


def access_token():
    refresh_mtime = _mtime("refresh_token.json")
    pin_mtime = _mtime("pin_login.json")
    if refresh_mtime is not None and (pin_mtime is None or refresh_mtime >= pin_mtime):
        return refresh_data()["accessToken"]
    return pin_data()["accessToken"]


def refresh_token():
    refresh_mtime = _mtime("refresh_token.json")
    pin_mtime = _mtime("pin_login.json")
    if refresh_mtime is not None and (pin_mtime is None or refresh_mtime >= pin_mtime):
        return refresh_data()["refreshToken"]
    return pin_data()["refreshToken"]


def session_id():
    return pin_data()["sessionId"]
