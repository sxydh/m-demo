"""推荐好友列表
GET /message/v2/StrikeUpRelevant/getRecommendFriendsList

ticket_id 优先读 _Temp/auto_login.json（登录链产物），没有则用内置兜底。
用法: python -m api.get_recommend_friends_list [page]
"""

import json
import sys
import urllib.parse

from common.config import DEVICE_ID, GET_HEADERS, OUTPUT_DIR, TIMEOUT
from common.http import save, send, signed_headers
from common.log import log
from common.sign import distinct_request_id, generate_signature

FALLBACK_TICKET_ID = (
    "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3OTAwNTkxOTUsInV1aWQiOiJiaGdhaGZhaGZpYWNhY2dlIiw"
    "iYXBwX2NvZGUiOjEsImNsb25lZCI6MX0.ntQ3gO2XLsswWCvyE3aTaHzTXGs_Uptz9s7X_lDGBrs"
)

page = sys.argv[1] if len(sys.argv) > 1 else "2"

auto_login_file = OUTPUT_DIR / "auto_login.json"
if auto_login_file.exists():
    ticket_id = json.loads(auto_login_file.read_text(encoding="utf-8"))["response"]["info"]["data"]["ticket_id"]
    log("ticket_id from", auto_login_file.name)
else:
    ticket_id = FALLBACK_TICKET_ID
    log("ticket_id from fallback constant")

sig = generate_signature(DEVICE_ID)
headers = signed_headers(GET_HEADERS, sig)

params = {
    "action": "SWIPE_UP",
    "source": "pull_up_to_load_more",
    "ticket_id": ticket_id,
    "page": page,
    "distinctRequestId": distinct_request_id(),
}
url = "https://gw-cn.jiaoliuqu.com/message/v2/StrikeUpRelevant/getRecommendFriendsList?" + urllib.parse.urlencode(
    params)

log("GET", url)
status, data = send(url, method="GET", headers=headers, timeout=TIMEOUT)
save("get_recommend_friends_list", status, {"url": url}, data)

assert status == 200, "HTTP %d: %s" % (status, data)
assert data.get("response_status") == "success", "response_status=%r body=%s" % (
    data.get("response_status"),
    data,
)
log(json.dumps(data, ensure_ascii=False, indent=2)[:2000])
