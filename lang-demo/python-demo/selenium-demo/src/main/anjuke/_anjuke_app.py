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
                append(f='cities', r=f'{city.get_attribute('href')}###{city.get_attribute('innerText').strip()}')
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
            city_split = city.split('###')
            city_href = city_split[0]
            city_name = city_split[1]
            cli.get(city_href)
            close_login(cli)
            nav_list = cli.find_elements_d(by=By.CSS_SELECTOR, value='.nav-channel-list li a', timeout=5, count=1)
            href = nav_list[0].get_attribute('href')
            if '/sale/' in href:
                continue
            append(f='new_houses', r=f'{href}###{city_name}')
        except Exception as e:
            append_e(f='new_houses_error', r=city)
            append_e(str(e))
    cli.close()


def pull_results(force=False):
    new_houses = read_rows(f='new_houses')
    if len(new_houses) == 0:
        return

    with get_sqlite_connection(f='anjuke.db') as conn:
        conn.execute('create table if not exists result(city text, province text, raw text, total int)')
        if force:
            conn.execute('delete from result')

        cli = Cli(undetected=False,
                  images_disabled=False,
                  headless=False)

        for new_house in new_houses:
            try:
                new_house_split = new_house.split('###')
                new_house_href = new_house_split[0]
                new_house_name = new_house_split[1]
                cli.get(f'{new_house_href.split('?')[0]}loupan/all/s1/')
                close_login(cli)
                results = cli.find_element_d(by=By.CSS_SELECTOR, value='.list-results', timeout=10, count=1, raise_e=False)
                if results:
                    results = results.get_attribute('innerHTML')
                    conn.execute(f'insert into result(city, raw) values(\'{new_house_name}\', \'{results}\')')
                    conn.commit()
            except Exception as e:
                append_e(f='mods_error', r=new_house)
                append_e(str(e))


if __name__ == '__main__':
    pull_cities()
    pull_new_houses()
    pull_results()
