"""推荐好友列表
GET /message/v2/StrikeUpRelevant/getRecommendFriendsList

依赖 _Temp/auto_login.json，先跑 python -m api.auto_login。
用法: python -m api.get_recommend_friends_list
"""

import json
import urllib.parse

from common.auth import ticket_id
from common.config import DEVICE_ID, GET_HEADERS, TIMEOUT
from common.http import save, send, signed_headers
from common.log import log
from common.sign import distinct_request_id, generate_signature

sig = generate_signature(DEVICE_ID)
headers = signed_headers(GET_HEADERS, sig)

params = {
    "action": "SWIPE_UP",
    "source": "pull_up_to_load_more",
    "ticket_id": ticket_id(),
    "page": "1",
    "distinctRequestId": distinct_request_id(),
}
url = "https://gw-cn.jiaoliuqu.com/message/v2/StrikeUpRelevant/getRecommendFriendsList?" + urllib.parse.urlencode(params)

log("GET", url)
status, data = send(url, method="GET", headers=headers, timeout=TIMEOUT)
save("get_recommend_friends_list", status, {"url": url}, data)

assert status == 200, "HTTP %d: %s" % (status, data)
assert data.get("response_status") == "success", "response_status=%r body=%s" % (
    data.get("response_status"),
    data,
)
log(json.dumps(data, ensure_ascii=False, indent=2)[:2000])
