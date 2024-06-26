import os
from base64 import urlsafe_b64encode

from cryptography.fernet import Fernet
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

salt = os.urandom(16)


def get_key(pwd):
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32,
        salt=salt,
        iterations=100000,
        backend=default_backend()
    )
    return urlsafe_b64encode(kdf.derive(pwd.encode()))


if __name__ == '__main__':
    password = input("Enter your password: ")

    key = get_key(password)
    cipher_suite = Fernet(key)

    text = "Hello, World!"

    cipher_text = cipher_suite.encrypt(text.encode())
    print("Encrypted: ", cipher_text)

    password = input("Enter your password: ")

    key = get_key(password)
    cipher_suite = Fernet(key)
    plain_text = cipher_suite.decrypt(cipher_text).decode()
    print("Decrypted: ", plain_text)
