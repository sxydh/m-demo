from base64 import urlsafe_b64encode

from cryptography.fernet import Fernet
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from m_pyutil import mtmp

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


def encrypt(password):
    key = get_key(password)
    cipher_suite = Fernet(key)

    decrypt_text = mtmp.read('decrypt.txt')
    encrypt_text = cipher_suite.encrypt(decrypt_text.encode())
    print('Encrypted: ', encrypt_text)

    mtmp.truncate('encrypt.txt')
    mtmp.append(encrypt_text.decode(), 'encrypt.txt')

    mtmp.truncate('decrypt.txt')


def decrypt(password):
    key = get_key(password)
    cipher_suite = Fernet(key)

    cipher_text = mtmp.read('encrypt.txt')
    decrypt_text = cipher_suite.decrypt(cipher_text).decode()
    print('Decrypted: ', decrypt_text)


if __name__ == '__main__':
    encrypt_pwd = input('Enter your encrypt password: ')
    if len(encrypt_pwd) != 0:
        encrypt(encrypt_pwd)

    decrypt_pwd = input('Enter your decrypt password: ')
    if len(decrypt_pwd) != 0:
        decrypt(decrypt_pwd)
