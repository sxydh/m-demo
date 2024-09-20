import re

from bs4 import BeautifulSoup

from src.main.util.common import get_sqlite_connection


def parse_raw():
    with get_sqlite_connection(f='anjuke.db') as conn:
        conn.execute('create table if not exists mod(name text, city text, price text, price_n int, address text, huxing text, tags text, sort text)')
        conn.execute('delete from mod')
        cursor = conn.execute('select city, raw from result')
        rows = cursor.fetchall()

        def helper(src, clazz, is_multi=False):
            if not is_multi:
                one = src.select_one(clazz)
                if one:
                    return one.text.strip()
                return ''
            else:
                multi = src.select(clazz)
                if multi:
                    return ', '.join([one.text.strip() for one in multi])
                return ''

        for row in rows:
            city = row[0]
            raw = row[-1]
            soup = BeautifulSoup(raw, 'html.parser')
            total = helper(soup, '.result')
            if len(total) > 0:
                total = re.search(r'(\d+)', total).group(1)
                total = int(total)
            else:
                total = 0
            conn.execute(f'update result set total = \'{total}\' where city = \'{city}\'')
            conn.commit()
            if total > 0:
                mods = soup.select('.item-mod')

                for mod in mods:
                    name = helper(mod, '.lp-name')
                    price = helper(mod, '.price')
                    price_n = ''
                    if len(price) > 0:
                        price_n = re.search(r'(\d+)', price).group(1)
                    address = helper(mod, '.address')
                    huxing = helper(mod, '.huxing')
                    tags = helper(mod, '.tag-panel i,span', is_multi=True)
                    sort = mod['data-soj']
                    conn.execute(f'insert into mod(name, city, price, price_n, address, huxing, tags, sort) values(\'{city}\', \'{name}\', \'{price}\', \'{price_n}\', \'{address}\', \'{huxing}\', \'{tags}\', \'{sort}\')')
                    conn.commit()


if __name__ == '__main__':
    parse_raw()
