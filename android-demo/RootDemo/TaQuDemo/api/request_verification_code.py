"""发送短信验证码
POST /jpush/v1/verificationCode/requestVerificationCode

真实调用会向 config.PHONE 下发短信。
用法: python -m api.request_verification_code
"""

import json
import urllib.parse

from common.config import DEVICE_ID, FORM_HEADERS, PHONE, TIMEOUT
from common.http import save, send, signed_headers
from common.log import log
from common.sign import distinct_request_id, generate_signature

sig = generate_signature(DEVICE_ID)
headers = signed_headers(FORM_HEADERS, sig)

url = (
        "https://gw-cn.jiaoliuqu.com/jpush/v1/verificationCode/requestVerificationCode?"
        + urllib.parse.urlencode({"distinctRequestId": distinct_request_id()})
)
body = urllib.parse.urlencode(
    {"sms_type": "SINGLE_VCODE", "code": "reg_login", "phone": PHONE}
).encode()

log("POST", url, body.decode())
status, data = send(url, method="POST", headers=headers, data=body, timeout=TIMEOUT)
save("request_verification_code", status, {"url": url, "body": body.decode()}, data)

assert status == 200, "HTTP %d: %s" % (status, data)
assert data.get("response_status") == "success", "response_status=%r body=%s" % (
    data.get("response_status"),
    data,
)
log(json.dumps(data, ensure_ascii=False, indent=2))
