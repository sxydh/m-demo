import base64
import secrets

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

ITERATIONS = 0x10000
KEY_LEN = 0x100 // 8


def derive_key(password, salt):
    return PBKDF2HMAC(
        algorithm=hashes.SHA256(), length=KEY_LEN, salt=salt, iterations=ITERATIONS
    ).derive(password.encode("utf-8"))


def encrypt(plain_text, password):
    iv = secrets.token_bytes(16)
    salt = secrets.token_bytes(16)
    ct = AESGCM(derive_key(password, salt)).encrypt(iv, plain_text.encode("utf-8"), None)
    return base64.b64encode(iv + salt + ct).decode("ascii")


def decrypt(encrypted_b64, password):
    blob = base64.b64decode(encrypted_b64)
    iv, salt, ct = blob[:16], blob[16:32], blob[32:]
    return AESGCM(derive_key(password, salt)).decrypt(iv, ct, None).decode("utf-8")
