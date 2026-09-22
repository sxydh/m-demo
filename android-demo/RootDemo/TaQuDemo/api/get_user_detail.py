"""用户详情(个人主页)
GET /i/v1/info/getHomepageInfoV3

accountUuid 可用命令行参数指定，缺省读 _Temp/auto_login.json 里自己的 uuid。
用法: python -m api.get_user_detail [accountUuid]
"""

import json
import sys
import urllib.parse

from common.config import DEVICE_ID, GET_HEADERS, OUTPUT_DIR, TIMEOUT
from common.http import save, send, signed_headers
from common.log import log
from common.sign import distinct_request_id, generate_signature

FALLBACK_UUID = "bhgahfahfiacacge"
FALLBACK_TICKET_ID = (
    "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3OTAwNTkxOTUsInV1aWQiOiJiaGdhaGZhaGZpYWNhY2dlIiw"
    "iYXBwX2NvZGUiOjEsImNsb25lZCI6MX0.ntQ3gO2XLsswWCvyE3aTaHzTXGs_Uptz9s7X_lDGBrs"
)

if len(sys.argv) > 1:
    account_uuid = sys.argv[1]
else:
    account_uuid = FALLBACK_UUID

auto_login_file = OUTPUT_DIR / "auto_login.json"
if auto_login_file.exists():
    data_ = json.loads(auto_login_file.read_text(encoding="utf-8"))["response"]["info"]["data"]
    ticket_id = data_["ticket_id"]
    if len(sys.argv) <= 1:
        account_uuid = data_["account_uuid"]
    log_src = auto_login_file.name
else:
    ticket_id = FALLBACK_TICKET_ID
    log_src = "fallback constant"

log("accountUuid:", account_uuid, "ticket from", log_src)

sig = generate_signature(DEVICE_ID)
headers = signed_headers(GET_HEADERS, sig)

params = {
    "accountUuid": account_uuid,
    "isReturnAvatar": "1",
    "ticket_id": ticket_id,
    "distinctRequestId": distinct_request_id(),
}
url = "https://gw-cn.jiaoliuqu.com/i/v1/info/getHomepageInfoV3?" + urllib.parse.urlencode(params)

log("GET", url)
status, data = send(url, method="GET", headers=headers, timeout=TIMEOUT)
save("get_user_detail", status, {"url": url}, data)

assert status == 200, "HTTP %d: %s" % (status, data)
assert data.get("response_status") == "success", "response_status=%r body=%s" % (
    data.get("response_status"),
    data,
)
log(json.dumps(data, ensure_ascii=False, indent=2)[:3000])
