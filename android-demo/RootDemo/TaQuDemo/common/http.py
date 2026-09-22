"""基于 stdlib 的极简 HTTP：签名头合并、请求、gzip/JSON 解码、结果落盘。"""

from __future__ import annotations

import gzip
import json
import time
import urllib.error
import urllib.request

from .config import DEVICE_ID, OUTPUT_DIR, TIMEOUT
from .log import log


def signed_headers(base: dict[str, str], sig: dict, device_id: str = DEVICE_ID) -> dict[str, str]:
    merged = dict(base)
    merged.update(
        {
            "to": device_id,
            "tp": str(int(time.time() * 1000)),
            "time": str(sig["_time_us"]),
            "xkey": sig["xkey"],
            "hkey": sig["hkey"],
            "ikey": sig["ikey"],
            "okey": sig["okey"],
            "tkey": sig["tkey"],
            "vaild": sig["vaild"],
        }
    )
    return merged


def send(
    url: str,
    *,
    method: str = "GET",
    headers: dict[str, str] | None = None,
    data: bytes | None = None,
    timeout: float = TIMEOUT,
) -> tuple[int, dict]:
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            raw, encoding, status = resp.read(), resp.headers.get("Content-Encoding"), resp.status
    except urllib.error.HTTPError as exc:
        raw, encoding, status = exc.read(), exc.headers.get("Content-Encoding"), exc.code
    if encoding == "gzip":
        raw = gzip.decompress(raw)
    text = raw.decode("utf-8", "replace")
    try:
        return status, json.loads(text)
    except json.JSONDecodeError:
        return status, {"_raw": text}


def save(name: str, status: int, request: dict, response: dict) -> dict:
    result = {"status": status, "request": request, "response": response}
    path = OUTPUT_DIR / f"{name}.json"
    path.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    log("saved", path, "HTTP", status)
    return result
