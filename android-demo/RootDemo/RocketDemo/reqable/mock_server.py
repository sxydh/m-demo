"""Reqable 本地 mock。

命中 apiId=3042：立刻返回固定响应，不访问真实上游。
其它 apiId：原样转发真实上游。

用法：
    python mock_server.py [port]      # 默认 8899

配合 Reqable Rewrite「重定向」把
    https://rocket-consumertxnservice.dutchbanglabank.com/apigw/v1/userService
重定向到
    http://127.0.0.1:8899/apigw/v1/userService
"""

import base64
import json
import os
import sys
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

import requests
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

TARGET_PATH = "/apigw/v1/userService"
UPSTREAM = "https://rocket-consumertxnservice.dutchbanglabank.com" + TARGET_PATH
OUTER_PASSWORD = "sdfheu$*sdfGw2"
PROXY = os.environ.get("MOCK_PROXY") or None


def _response_body(user_data):
    return json.dumps({"userData": user_data}, separators=(",", ":")).encode("utf-8")


RESPONSES = {
    3042: _response_body(
        "3bQlFXlPvrQv0I0ZsU5NBUzaKcObzB3RQ2UXkHHda94AZv/hrUkt22tRYm9w6vDkjiDem0sJbAmFJWwpEH7ILMyqt+2Klv8WDOf7KyU2oFC6exiaQUdGvc2HfekVYgNGUm2kJ62qEOH3+6iRnvU5lKOtKwBKYrUO6TgFV8WaI+bHdkvaZVqjzGDzFhTc554EYO1+6VIDFoccvhyonxZVpjS0sGIptU1t/tuchZhp5jOD8cOVue+xqXcrawHhBXta6G6RjA4IgkRk/SSxZhhg/WnumMBJdXIf2WxR2JAX/A=="
    ),
    3032: _response_body(
        "ZkVycqqGnw6/YKH8TIkjaw7brAUw2Vum/gqSOG25jikj7Glckzm2Y+/Ce7lxf/cRWW/kL0HsrxA3mB0C62QFZCtDPhP751GB+PuPrHC9VcosuZs5XKi2f/NoxY0jg/+/C9ci5MN631xvy9oS0+vjZGbrNFwgAMBSU9UHAxKITa3Q29xPDn2qBKlhbsFbFCOBft7KM0PnTYn0+VJAh7q4mN8IVS7UfSAVD3Wop7385P8S95SS8Ol07V/mOuJi+y+i/ptMLRkd+h6AI2HkoVJy+/eeAVx2wved2oK6oC+CDLEV5iYLi/LEQJp25fdfxiP9Igr6"
    ),
    3057: _response_body(
        "WySZwlqtsa9TImgi5PqIO661im9hAWJUKZoEUIwQZzJk/QpZMMlJcIHSKHpK/kyXXgsW0AsfU68VhZ0tM5UDsuolkkXqjKpmePbL/Ng/ZSK3"
    ),
}


def _derive_key(salt):
    return PBKDF2HMAC(
        algorithm=hashes.SHA256(), length=32, salt=salt, iterations=65536
    ).derive(OUTER_PASSWORD.encode("utf-8"))


def _decrypt(b64):
    blob = base64.b64decode(b64)
    iv, salt, ct = blob[:16], blob[16:32], blob[32:]
    return AESGCM(_derive_key(salt)).decrypt(iv, ct, None).decode("utf-8")


def _api_id(body):
    return json.loads(_decrypt(json.loads(body)["userData"])).get("apiId")


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, fmt, *args):
        sys.stdout.write("[mock] " + (fmt % args) + "\n")
        sys.stdout.flush()

    def _read_body(self):
        length = int(self.headers.get("Content-Length", 0))
        return self.rfile.read(length) if length else b""

    def _send(self, code, body, content_type):
        if isinstance(body, str):
            body = body.encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        body = self._read_body()
        if self.path.split("?")[0] != TARGET_PATH:
            self._send(404, "not found", "text/plain")
            return
        try:
            api_id = _api_id(body)
        except Exception as exc:
            self.log_message("decrypt failed: %r", exc)
            api_id = None
        if api_id in RESPONSES:
            body_out = RESPONSES[api_id]
            self.log_message("apiId=%s -> mock response (%d bytes)", api_id, len(body_out))
            self._send(200, body_out, "application/json")
            return
        self.log_message("apiId=%r -> forward upstream", api_id)
        headers = {
            "Content-Type": self.headers.get("Content-Type", "application/json"),
            "User-Agent": self.headers.get("User-Agent", "okhttp/4.9.3"),
        }
        for name in ("Authorization", "CHANNEL_ID"):
            if self.headers.get(name):
                headers[name] = self.headers.get(name)
        try:
            resp = requests.post(UPSTREAM, data=body, headers=headers, timeout=60, proxies=PROXY)
            self._send(
                resp.status_code,
                resp.content,
                resp.headers.get("Content-Type", "application/json"),
            )
        except Exception as exc:
            self.log_message("upstream error: %r", exc)
            self._send(502, json.dumps({"error": str(exc)}), "application/json")


def main():
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8899
    server = ThreadingHTTPServer(("127.0.0.1", port), Handler)
    print("mock listening on http://127.0.0.1:%d%s" % (port, TARGET_PATH))
    server.serve_forever()


if __name__ == "__main__":
    main()
