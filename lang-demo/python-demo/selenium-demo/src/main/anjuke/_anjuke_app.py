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
            if '/sale/' in href:
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

    with get_sqlite_connection(f='results.db') as conn:
        conn.execute('create table if not exists results(new_house text, raw text)')
        if force:
            conn.execute('delete from results')

        cli = Cli(undetected=False,
                  images_disabled=False,
                  headless=False)

        for new_house in new_houses:
            try:
                new_house = new_house.split('?')[0]
                cli.get(f'{new_house}loupan/all/s1/')
                close_login(cli)
                results = cli.find_element_d(by=By.CSS_SELECTOR, value='.list-results', timeout=10, count=1, raise_e=False)
                if results:
                    results = results.get_attribute('innerHTML')
                    conn.execute(f'insert into results(new_house, raw) values(\'{new_house}\', \'{results}\')')
                    conn.commit()
            except Exception as e:
                append_e(f='mods_error', r=new_house)
                append_e(str(e))


if __name__ == '__main__':
    pull_cities()
    pull_new_houses()
    pull_mods()
