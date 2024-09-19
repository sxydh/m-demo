import datetime
import os
import sqlite3
import threading

import pymysql

rlock = threading.RLock()


def add_days(ds, delta):
    d = datetime.datetime.strptime(ds, '%Y-%m-%d')
    return (d + datetime.timedelta(days=delta)).strftime('%Y-%m-%d')


def write(r, mode, f):
    with rlock:
        os.makedirs('tmp', exist_ok=True)
        with open(f'tmp/{f}', mode=mode, encoding='utf-8') as o:
            o.write(r)


def append(r, f='output.txt'):
    write(r=f'{r}\n', mode='a+', f=f)


def append_e(r, f='error.txt'):
    append(r=r, f=f)


def truncate(f):
    write(r='', mode='w', f=f)


def read(f):
    with rlock:
        os.makedirs('tmp', exist_ok=True)
        f = f'tmp/{f}'
        with open(f, mode='a', encoding='utf-8') as _:
            pass
        with open(f, mode='r', encoding='utf-8') as i:
            return i.read()


def read_rows(f='input.txt'):
    ft = read(f)
    if ft == '':
        return []
    return ft.split('\n')


def rename(f, new_f):
    with rlock:
        os.rename(f'tmp/{f}', f'tmp/{new_f}')


def remove(f):
    with rlock:
        os.remove(f'tmp/{f}')


def get_mysql_connection():
    return pymysql.connect(
        host='192.168.233.129',
        port=3306,
        user='root',
        password='123',
        db='ssq',
        charset='utf8mb4'
    )


def get_sqlite_connection():
    return sqlite3.connect('tmp/ssq.db')
