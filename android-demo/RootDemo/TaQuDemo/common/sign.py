"""unidbg 离线签名 (libnative-xbp.so)。

签名只依赖 tag/time_us/device_id，与具体 URL 无关，所有接口共用。
"""

from __future__ import annotations

import hashlib
import json
import os
import random
import subprocess
import time

from .config import CP_FILE, DEVICE_ID, SO_PATH, UNIDBG_DIR
from .log import timing


def _classpath() -> str:
    deps = CP_FILE.read_text(encoding="utf-8").strip()
    return str(UNIDBG_DIR / "target" / "classes") + os.pathsep + deps


def generate_signature(device_id: str = DEVICE_ID, time_us: int | None = None) -> dict:
    t0 = time.perf_counter()
    if time_us is None:
        time_us = int(time.time() * 1_000_000)
    cp = _classpath()
    t0 = timing("generate_signature.classpath", time.perf_counter() - t0, t0)
    proc = subprocess.run(
        ["java", "-cp", cp, "app.TaQuSign", str(SO_PATH), "GW", str(time_us), device_id],
        capture_output=True,
        text=True,
        timeout=180,
    )
    timing("generate_signature.java-subprocess(unidbg)", time.perf_counter() - t0, t0)
    for line in proc.stdout.splitlines():
        if line.startswith("SIGJSON="):
            sig = json.loads(line[len("SIGJSON="):])
            sig["_time_us"] = time_us
            return sig
    raise RuntimeError(f"no SIGJSON\nSTDOUT:\n{proc.stdout}\nSTDERR:\n{proc.stderr}")


def distinct_request_id(device_id: str = DEVICE_ID) -> str:
    raw = device_id + str(random.randint(0, 999)) + str(int(time.time() * 1000))
    return hashlib.md5(raw.encode()).hexdigest()
