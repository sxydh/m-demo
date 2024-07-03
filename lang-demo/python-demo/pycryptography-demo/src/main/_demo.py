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


def encrypt(password):
    key = get_key(password)
    cipher_suite = Fernet(key)

    text: str
    with open('tmp/input.txt', 'r+') as ifile:
        text = ifile.read()

        cipher_text = cipher_suite.encrypt(text.encode())
        print('Encrypted: ', cipher_text)

        with open('tmp/output.txt', 'w') as ofile:
            ofile.write(cipher_text.decode())

        ifile.write('')


def decrypt(password):
    key = get_key(password)
    cipher_suite = Fernet(key)

    cipher_text: str
    with open('tmp/output.txt') as file:
        cipher_text = file.read()

    text = cipher_suite.decrypt(cipher_text).decode()
    print('Decrypted: ', text)


if __name__ == '__main__':
    epwd = input('Enter your encrypt password: ')

    if len(epwd) != 0:
        encrypt(epwd)

    dpwd = input('Enter your decrypt password: ')

    if len(dpwd) != 0:
        decrypt(dpwd)
