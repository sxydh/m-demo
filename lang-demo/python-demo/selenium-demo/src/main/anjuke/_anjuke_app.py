from selenium.webdriver.common.by import By

from src.main.util.cli import Cli
from src.main.util.common import append_e, append, read_rows, truncate, get_sqlite_connection


def close_login(cli: Cli):
    pass


def pull_cities(force=False):
    cities = read_rows(f='cities')
    if not force and len(cities) > 0:
        return
    else:
        truncate(f='cities')

    cli = Cli(undetected=False,
              images_disabled=False,
              headless=False)
    while True:
        try:
            cli.get('https://www.anjuke.com/sy-city.html?from=HomePage_City')
            close_login(cli)
            cities = cli.find_elements_d(by=By.CSS_SELECTOR, value='.ajk-city-cell.is-letter li a')
            for city in cities:
                append(f='cities', r=city.get_attribute('href'))
            break
        except Exception as e:
            append_e(str(e))
    cli.close()


def pull_new_houses(force=False):
    cities = read_rows(f='cities')
    if len(cities) == 0:
        return
    new_houses = read_rows(f='new_houses')
    if not force and len(new_houses) > 0:
        return
    else:
        truncate(f='new_houses')

    cli = Cli(undetected=False,
              images_disabled=False,
              headless=False)
    for city in cities:
        try:
            cli.get(city)
            close_login(cli)
            nav_list = cli.find_elements_d(by=By.CSS_SELECTOR, value='.nav-channel-list li a', timeout=5, count=1)
            href = nav_list[0].get_attribute('href')
            if href.index('/sale/') >= 0:
                continue
            append(f='new_houses', r=href)
        except Exception as e:
            append_e(f='new_houses_error', r=city)
            append_e(str(e))
    cli.close()


# noinspection SqlDialectInspection,SqlNoDataSourceInspection
def pull_mods(force=False):
    new_houses = read_rows(f='new_houses')
    if len(new_houses) == 0:
        return

    with get_sqlite_connection(f='mods.db') as conn:
        conn.execute('create table if not exists mods(id integer primary key autoincrement, new_house text, name text, address text, huxing text, price text, tags text, raw text)')
        if force:
            conn.execute('delete from mods')

        cli = Cli(undetected=False,
                  images_disabled=False,
                  headless=False)

        for new_house in new_houses:
            try:
                new_house = new_house.split('?')[0]
                if new_houses.index('sale') >= 0:
                    conn.execute(f'insert into mods(new_house) values(\'{new_house}\')')
                    continue
                cli.get(f'{new_house}loupan/all/s1/')
                close_login(cli)
                empty = cli.find_element_d(by=By.CSS_SELECTOR, value='.empty', timeout=0, count=1, raise_e=False)
                if empty:
                    conn.execute(f'insert into mods(new_house) values(\'{new_house}\')')
                    continue
                results = cli.find_element_d(by=By.CSS_SELECTOR, value='.list-results', timeout=10, count=1, raise_e=False)
                if results:
                    results = results.get_attribute('innerHTML')
                    conn.execute(f'insert into mods(new_house, raw) values(\'{new_house}\', \'{results}\')')
                    conn.commit()
            except Exception as e:
                append_e(f='mods', r=new_house)
                append_e(str(e))


if __name__ == '__main__':
    pull_cities()
    pull_new_houses()
    pull_mods()
