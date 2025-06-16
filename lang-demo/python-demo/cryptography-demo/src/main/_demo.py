from base64 import urlsafe_b64encode

from cryptography.fernet import Fernet
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from m_pyutil import mtmp

# 不要删除
salt: bytes = b'20240626'


def get_key(pwd: str) -> bytes:
    kdf: PBKDF2HMAC = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32,
        salt=salt,
        iterations=100000,
        backend=default_backend()
    )
    return urlsafe_b64encode(kdf.derive(pwd.encode()))


def encrypt(decryption_text: str, password: str) -> str | None:
    decryption_text = decryption_text.strip()
    if decryption_text == '':
        return None

    key: bytes = get_key(password)
    cipher_suite: Fernet = Fernet(key)

    return cipher_suite.encrypt(decryption_text.encode()).decode()


def decrypt(encryption_text: str, password: str) -> str | None:
    encryption_text = encryption_text.strip()
    if encryption_text == '':
        return None

    key: bytes = get_key(password)
    cipher_suite: Fernet = Fernet(key)

    return cipher_suite.decrypt(encryption_text).decode()


def start():
    encryption_pwd: str = input('Enter encryption password: ')
    if len(encryption_pwd) != 0:
        mtmp.truncate('encryption.txt')
        decryption_text = mtmp.read('decryption.txt')
        mtmp.truncate('decryption.txt')
        print('Encryption: ')
        for line in decryption_text.split('\n'):
            encryption_text: str = encrypt(line, encryption_pwd)
            if encryption_text is not None:
                print(encryption_text)
                mtmp.append(encryption_text, 'encryption.txt')

    decryption_pwd = input('Enter decryption password: ')
    if len(decryption_pwd) != 0:
        mtmp.truncate('decryption.txt')
        encryption_text: str = mtmp.read('encryption.txt')
        print('Decryption: ')
        for line in encryption_text.split('\n'):
            decryption_text: str = decrypt(line, decryption_pwd)
            if decryption_text is not None:
                print(decryption_text)


if __name__ == '__main__':
    start()
