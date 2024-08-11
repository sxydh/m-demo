from pymysql import Connection


def get_conn():
    return Connection(host='192.168.233.129', port=3306, user='root', password='123')


if __name__ == '__main__':
    with get_conn() as conn:
        with conn.cursor() as cursor:
            cursor.execute(' create database if not exists py_demo ')
            conn.commit()
            conn.select_db('py_demo')
            cursor.execute(' create table if not exists '
                           ' app_log(log_id bigint primary key, ip varchar(100), op varchar(20), val varchar(255)) ')
            conn.commit()
