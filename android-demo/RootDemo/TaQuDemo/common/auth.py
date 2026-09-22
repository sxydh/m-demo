"""登录态：从 _Temp/auto_login.json 读取（由 api.auto_login 产出）。"""

from __future__ import annotations

import json

from .config import OUTPUT_DIR


def login_data() -> dict:
    """返回登录响应 info.data（含 ticket_id / account_uuid 等）。"""
    path = OUTPUT_DIR / "auto_login.json"
    if not path.exists():
        raise FileNotFoundError(f"{path} 不存在，请先运行: python -m api.auto_login")
    data = json.loads(path.read_text(encoding="utf-8"))
    account = data["response"]["info"]["data"]
    if not account or not account.get("ticket_id"):
        raise RuntimeError(f"{path} 里没有有效 ticket_id，请重新登录")
    return account


def ticket_id() -> str:
    return login_data()["ticket_id"]
