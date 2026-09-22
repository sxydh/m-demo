"""spawn 注入他趣并脱壳。

参考 tig3rhu《他趣APP脱壳》：先用 hook_libnesec.js 压掉网易易盾的反 Frida 线程，
再挂 dump_dex.js（hook ClassLinker::DefineClass）落盘 dex。

用法：
    python run_dump.py                 # 用默认 host/pkg
    python run_dump.py --host 127.0.0.1:31337 --pkg com.xingjiabi.shengsheng
"""

from __future__ import annotations

import argparse
import os
import sys
import time

import frida

HERE = os.path.dirname(os.path.abspath(__file__))
DEFAULT_HOST = "127.0.0.1:31337"
DEFAULT_PKG = "com.xingjiabi.shengsheng"
SCRIPTS = ["hook_libnesec.js", "dump_dex.js"]


def on_message(message, data):
    if message.get("type") == "send":
        payload = message.get("payload")
        print(f"[send] {payload}", flush=True)
    elif message.get("type") == "error":
        print(f"[error] {message.get('description')}", flush=True)
        stack = message.get("stack")
        if stack:
            print(stack, flush=True)
    else:
        print(f"[msg] {message}", flush=True)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default=DEFAULT_HOST)
    parser.add_argument("--pkg", default=DEFAULT_PKG)
    args = parser.parse_args()

    device = frida.get_device_manager().add_remote_device(args.host)
    print(f"[*] device = {device}", flush=True)

    pid = device.spawn([args.pkg])
    print(f"[*] spawned {args.pkg} pid={pid}", flush=True)

    session = device.attach(pid)
    print(f"[*] attached session pid={pid}", flush=True)

    for name in SCRIPTS:
        path = os.path.join(HERE, name)
        with open(path, "r", encoding="utf-8") as fh:
            source = fh.read()
        script = session.create_script(source)
        script.on("message", on_message)
        script.load()
        print(f"[*] loaded {name}", flush=True)
        time.sleep(0.3)

    device.resume(pid)
    print("[*] resumed; Ctrl+C to stop", flush=True)

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("[*] stopping", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
