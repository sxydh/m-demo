"""极简日志：直接用 print 输出，无日志库。"""

from __future__ import annotations

import time


def log(*args, sep: str = " ", end: str = "\n") -> None:
    print(*args, sep=sep, end=end, flush=True)


def timing(label: str, cost: float, start: float) -> float:
    """打一条耗时日志，返回当前 perf_counter 便于链式分段计时。"""
    now = time.perf_counter()
    log(f"[TIMING] {label}: {cost * 1000:.1f} ms (累计 {(now - start) * 1000:.1f} ms)")
    return now
