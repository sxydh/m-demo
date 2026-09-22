"""验证码登录
POST /i/v1/Account/autoLogin

校验验证码并登录，成功后 ticket 落到 _Temp/auto_login.json，供后续接口读取。
用法: python -m api.auto_login    （交互式输入验证码）
"""

import json
import urllib.parse

from common.config import (
    DEVICE_ID,
    FORM_HEADERS,
    LOGIN_EXTRA_JSON,
    PHONE,
    SHUMEI_DEVICE_ID,
    TIMEOUT,
)
from common.http import save, send, signed_headers
from common.log import log
from common.sign import distinct_request_id, generate_signature

vcode = input("vcode: ").strip()

sig = generate_signature(DEVICE_ID)
headers = signed_headers(FORM_HEADERS, sig)

url = (
    "https://gw-cn.jiaoliuqu.com/i/v1/Account/autoLogin?"
    + urllib.parse.urlencode({"spm": "0", "distinctRequestId": distinct_request_id()})
)
body = urllib.parse.urlencode(
    {
        "mobile": PHONE,
        "type": "0",
        "vcode": vcode,
        "version_number": "1",
        "extra_json": LOGIN_EXTRA_JSON,
        "shumei_device_id": SHUMEI_DEVICE_ID,
    }
).encode()

log("POST", url, body.decode())
status, data = send(url, method="POST", headers=headers, data=body, timeout=TIMEOUT)
save("auto_login", status, {"url": url, "body": body.decode()}, data)

assert status == 200, "HTTP %d: %s" % (status, data)
assert data.get("response_status") == "success", "response_status=%r body=%s" % (
    data.get("response_status"),
    data,
)
account = data["info"]["data"]
log("ticket_id:", account["ticket_id"])
log("nickname:", account["nickname"], "mobile:", account["mobile"])
