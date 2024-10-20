import os
import sqlite3


def get_sqlite_connection(f='boss.db'):
    os.makedirs('tmp', exist_ok=True)
    return sqlite3.connect(f'tmp/{f}')
