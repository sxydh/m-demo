"""查询流水
POST /apigw/v1/userService
"""

import json
from datetime import datetime, timedelta

from common.cipher import decrypt
from common.client import bearer, post
from common.config import (
    BASE_URL_LOCATION,
    CLIENT_LANGUAGE,
    CLIENT_VERSION,
    DEVICE_ID,
    DEVICE_INFO,
    PHONE,
    ROCKET_SESSION_ID,
)
from common.log import log
from common.session import access_token, security_code, session_id

secret_key = security_code()

now = datetime.now()
payload = {
    "apiId": 3008,
    "deviceId": "ANDROID-%s" % DEVICE_ID,
    "deviceInfo": DEVICE_INFO,
    "clientVersion": CLIENT_VERSION,
    "refId": "agent-statement-%s-%s%03d"
    % (PHONE, now.strftime("%Y%m%d%H%M%S"), now.microsecond // 1000),
    "sessionId": session_id(),
    "clientLanguage": CLIENT_LANGUAGE,
    "data": {
        "appType": "C",
        "initiatorId": PHONE,
        "rocketSessionId": ROCKET_SESSION_ID,
        "userId": PHONE,
        "version": CLIENT_VERSION,
        "svaNo": PHONE + "0",
        "reportType": "Txn",
        "fromDate": (now - timedelta(days=23)).strftime("%d/%m/%Y"),
        "toDate": now.strftime("%d/%m/%Y"),
    },
}

url = BASE_URL_LOCATION.rstrip("/") + "/apigw/v1/userService"
resp = post(
    url,
    payload,
    bearer(access_token()),
    "agent_statement.json",
    "查询流水",
    password=secret_key,
)

assert resp.status_code == 200, "HTTP %d: %s" % (resp.status_code, resp.text)
data = json.loads(decrypt(resp.json()["userData"], secret_key))
log(json.dumps(data, ensure_ascii=False, indent=2))
