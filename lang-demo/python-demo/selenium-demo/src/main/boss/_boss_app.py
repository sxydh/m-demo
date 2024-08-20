import sys
import threading
import time
from datetime import datetime
from time import sleep
from urllib.parse import quote

from selenium.webdriver.common.by import By

from src.main.util.cli import Cli
from src.main.util.common import append, append_e, read_rows


def login_close(cli: Cli):
    close = cli.find_element_d(by=By.CSS_SELECTOR, value='[ka="boss-login-close"]', timeout=0, count=1, raise_e=False)
    if close is not None:
        cli.click(close)


def pull_cities(cli: Cli):
    cities = read_rows('cities')
    if len(cities) > 0 and cities[0] != '':
        return

    # 重试循环
    while True:
        try:
            cli.get('https://www.zhipin.com/')
            switchover_city = cli.find_element_d(by=By.CSS_SELECTOR, value='.switchover-city')
            login_close(cli)
            cli.click(switchover_city)
            city_group_section = cli.find_element_d(by=By.CSS_SELECTOR, value='.city-group-section')
            city_group_item_list = cli.find_elements(src=city_group_section, by=By.CSS_SELECTOR, value='.city-group-item ul a')
            cities = []
            cities_with_chs = []
            for (_, city_group_item) in enumerate(city_group_item_list):
                ka = city_group_item.get_attribute('ka')
                cities.append(ka.split('_')[-1])
                text = city_group_item.get_attribute('innerText').strip()
                cities_with_chs.append(f'{ka},{text}')
            append(r=str.join('\n', cities), f='cities')
            append(r=str.join('\n', cities_with_chs), f='cities_with_chs')
            break
        except Exception as e:
            append_e(str(e))
            sleep(1)


def pull_queries(cli):
    queries = read_rows('queries')
    if len(queries) > 0 and queries[0] != '':
        return

    # 重试循环
    while True:
        try:
            cli.get('https://www.zhipin.com/web/geek/job')
            condition_position_select = cli.find_element_d(by=By.CSS_SELECTOR, value='.condition-position-select')
            login_close(cli)
            cli.click(condition_position_select)
            sel_position_list = cli.find_elements(src=condition_position_select, by=By.CSS_SELECTOR, value='li')
            queries = []
            for (i, sel_position) in enumerate(sel_position_list):
                if i == 0:
                    continue
                cli.click(sel_position)
                position_list = cli.find_elements(src=condition_position_select, by=By.CSS_SELECTOR, value='.condition-position-list a')
                for position in position_list:
                    queries.append(position.get_attribute('innerText'))
            append(r=str.join('\n', queries), f='queries')
            break
        except Exception as e:
            append_e(str(e))
            sleep(1)


def pull_jobs(cli: Cli):
    cities = read_rows('cities')
    queries = read_rows('queries')

    for city in cities:

        if city == 'return':
            return

        for query in queries:

            if query == 'return':
                return

            doing_flag = f'{city},{query},{datetime.now().hour}'
            done_flag = f'{city},{query}'

            start_time = time.time()
            jobs = read_rows('jobs')
            diff_time = time.time() - start_time
            if diff_time >= 1:
                append_e(f'{doing_flag} => read_rows.diff_time = {diff_time}')
            if doing_flag in jobs or done_flag in jobs:
                continue

            append(r=doing_flag, f='jobs')

            # 分页循环
            page = 1
            n = 1
            while page <= n:

                # 重试循环
                while True:
                    try:
                        append(r=f'{doing_flag} => page = {page}', f='logs')
                        cli.get(f'https://www.zhipin.com/web/geek/job?query={quote(query)}&city={quote(city)}&page={page}')
                        job_empty_box = cli.find_element_d(by=By.CSS_SELECTOR, value='.job-empty-box', timeout=1, raise_e=False)
                        if job_empty_box is not None:
                            break
                        options_pages = cli.find_elements_d(by=By.CSS_SELECTOR, value='.options-pages a')
                        if len(options_pages) > 2:
                            n = int(options_pages[-2].get_attribute('innerText'))

                        # 解析内容
                        login_close(cli)
                        jobs = cli.find_elements_d(by=By.CSS_SELECTOR, value='.job-list-box .job-card-wrapper')
                        for job in jobs:
                            append(f'###########{job.get_attribute('innerHTML')}')
                        break
                    except Exception as e:
                        append_e(str(e))
                        sleep(1)

                page += 1
                sleep(1)

            append(r=done_flag, f='jobs')


def init():
    cli = Cli(undetected=True,
              headless=False,
              proxy=proxy,
              images_disabled=True)

    pull_cities(cli)
    pull_queries(cli)

    sleep(1)
    cli.close()


def start():
    init()

    def pull_jobs_task():
        pull_jobs(cli=Cli(undetected=True,
                          headless=False,
                          proxy=proxy,
                          images_disabled=True))

    for _ in range(10):
        sleep(2)
        threading.Thread(target=pull_jobs_task).start()


if __name__ == '__main__':
    proxy = None
    if len(sys.argv) > 1:
        proxy = sys.argv[1]

    start()
