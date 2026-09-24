from reqable import *

import base64
import json

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

OUTER_PASSWORD = "sdfheu$*sdfGw2"
REPLACEMENTS = {
    3032: "ZkVycqqGnw6/YKH8TIkjaw7brAUw2Vum/gqSOG25jikj7Glckzm2Y+/Ce7lxf/cRWW/kL0HsrxA3mB0C62QFZCtDPhP751GB+PuPrHC9VcosuZs5XKi2f/NoxY0jg/+/C9ci5MN631xvy9oS0+vjZGbrNFwgAMBSU9UHAxKITa3Q29xPDn2qBKlhbsFbFCOBft7KM0PnTYn0+VJAh7q4mN8IVS7UfSAVD3Wop7385P8S95SS8Ol07V/mOuJi+y+i/ptMLRkd+h6AI2HkoVJy+/eeAVx2wved2oK6oC+CDLEV5iYLi/LEQJp25fdfxiP9Igr6",
    3042: "3bQlFXlPvrQv0I0ZsU5NBUzaKcObzB3RQ2UXkHHda94AZv/hrUkt22tRYm9w6vDkjiDem0sJbAmFJWwpEH7ILMyqt+2Klv8WDOf7KyU2oFC6exiaQUdGvc2HfekVYgNGUm2kJ62qEOH3+6iRnvU5lKOtKwBKYrUO6TgFV8WaI+bHdkvaZVqjzGDzFhTc554EYO1+6VIDFoccvhyonxZVpjS0sGIptU1t/tuchZhp5jOD8cOVue+xqXcrawHhBXta6G6RjA4IgkRk/SSxZhhg/WnumMBJdXIf2WxR2JAX/A==",
    3057: "WySZwlqtsa9TImgi5PqIO661im9hAWJUKZoEUIwQZzJk/QpZMMlJcIHSKHpK/kyXXgsW0AsfU68VhZ0tM5UDsuolkkXqjKpmePbL/Ng/ZSK3",
}


def _replacement_body(api_id):
    return json.dumps({"userData": REPLACEMENTS[api_id]}, separators=(",", ":"))


def _derive_key(password, salt):
    return PBKDF2HMAC(
        algorithm=hashes.SHA256(), length=32, salt=salt, iterations=65536
    ).derive(password.encode("utf-8"))


def _decrypt(b64):
    blob = base64.b64decode(b64)
    iv, salt, ct = blob[:16], blob[16:32], blob[32:]
    return AESGCM(_derive_key(OUTER_PASSWORD, salt)).decrypt(iv, ct, None).decode("utf-8")


def onRequest(context, request):
    return request


def onResponse(context, response):
    try:
        body = response.request.body
        if body.isText:
            user_data = json.loads(body.payload).get("userData")
            if user_data:
                plain = json.loads(_decrypt(user_data))
                api_id = plain.get("apiId")
                if api_id in REPLACEMENTS:
                    body = _replacement_body(api_id)
                    response.body = body
                    if response.headers["content-length"] is not None:
                        response.headers["content-length"] = str(
                            len(body.encode("utf-8"))
                        )
                    context.highlight = Highlight.green
                    print("[Rocket] rewrite response for apiId=%s" % api_id)
    except Exception as e:
        print("[Rocket] script error:", repr(e))
    return response
