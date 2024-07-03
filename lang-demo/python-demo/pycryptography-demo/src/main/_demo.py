from base64 import urlsafe_b64encode

from cryptography.fernet import Fernet
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

# 不要删除
salt = b'20240626'


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
    password = input('Enter your encrypt password: ')

    if len(password) != 0:
        key = get_key(password)
        cipher_suite = Fernet(key)

        text: str
        with open('tmp/input.txt') as file:
            text = file.read()

        cipher_text = cipher_suite.encrypt(text.encode())
        print('Encrypted: ', cipher_text)

        with open('tmp/output.txt', 'x') as file:
            file.write(cipher_text.decode())

    password = input('Enter your decrypt password: ')

    if len(password) != 0:
        key = get_key(password)
        cipher_suite = Fernet(key)

        cipher_text: str
        with open('tmp/output.txt') as file:
            cipher_text = file.read()

        text = cipher_suite.decrypt(cipher_text).decode()
        print('Decrypted: ', text)

        with open('tmp/input.txt', 'w') as file:
            file.write(text)
