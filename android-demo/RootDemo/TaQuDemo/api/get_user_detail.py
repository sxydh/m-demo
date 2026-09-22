"""用户详情(个人主页)
GET /i/v1/info/getHomepageInfoV3

依赖 _Temp/auto_login.json，先跑 python -m api.auto_login。
用法: python -m api.get_user_detail
"""

import json
import urllib.parse

from common.auth import login_data
from common.config import DEVICE_ID, GET_HEADERS, TIMEOUT
from common.http import save, send, signed_headers
from common.log import log
from common.sign import distinct_request_id, generate_signature

account = login_data()
ticket = account["ticket_id"]
account_uuid = account["account_uuid"]  # 查他人详情: 换成目标 uuid

sig = generate_signature(DEVICE_ID)
headers = signed_headers(GET_HEADERS, sig)

params = {
    "accountUuid": account_uuid,
    "isReturnAvatar": "1",
    "ticket_id": ticket,
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
