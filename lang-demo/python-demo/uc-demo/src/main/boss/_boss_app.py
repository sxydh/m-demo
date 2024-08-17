import random
from time import sleep
from urllib.parse import quote

from selenium.webdriver.common.by import By

from src.main.util.chrome_cli import ChromeCli
from src.main.util.common import append, append_e, read_rows


def pull_cities():
    cities = read_rows('cities')
    if len(cities) > 0 and cities[0] != '':
        return
    # 重试循环
    while True:
        try:
            chrome_cli.get('https://www.zhipin.com/')
            switchover_city = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='.switchover-city')
            login_close()
            chrome_cli.click(switchover_city)
            city_group_section = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='.city-group-section')
            city_group_item_list = chrome_cli.find_elements(src=city_group_section, by=By.CSS_SELECTOR, value='.city-group-item ul a')
            cities = []
            cities_with_chs = []
            for (i, city_group_item) in enumerate(city_group_item_list):
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


def pull_queries():
    queries = read_rows('queries')
    if len(queries) > 0 and queries[0] != '':
        return
    # 重试循环
    while True:
        try:
            chrome_cli.get('https://www.zhipin.com/web/geek/job')
            condition_position_select = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='.condition-position-select')
            login_close()
            chrome_cli.click(condition_position_select)
            sel_position_list = chrome_cli.find_elements(src=condition_position_select, by=By.CSS_SELECTOR, value='li')
            queries = []
            for (i, sel_position) in enumerate(sel_position_list):
                if i == 0:
                    continue
                chrome_cli.click(sel_position)
                position_list = chrome_cli.find_elements(src=condition_position_select, by=By.CSS_SELECTOR, value='.condition-position-list a')
                for position in position_list:
                    queries.append(position.get_attribute('innerText'))
            append(r=str.join('\n', queries), f='queries')
            break
        except Exception as e:
            append_e(str(e))
            sleep(1)


def pull_jobs():
    cities = read_rows('cities')
    queries = read_rows('queries')
    jobs = read_rows('jobs')

    for city in cities:
        for query in queries:
            if f'{city},{query}' in jobs:
                continue
            if random.choice([False, True, False]):
                chrome_cli.get(random.choice([
                    'https://www.zhipin.com/web/geek/job-recommend',
                    'https://www.zhipin.com/gongsi',
                    'https://www.zhipin.com/overseas/',
                    'https://app.zhipin.com/'
                ]))
                sleep(random.choice([1, 1, 1, 2, 3]))

            # 分页循环
            page = 1
            n = 1
            while page <= n:

                # 重试循环
                while True:
                    try:
                        chrome_cli.get(f'https://www.zhipin.com/web/geek/job?query={quote(query)}&city={quote(city)}&page={page}')
                        job_empty_box = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='.job-empty-box', timeout=1, raise_e=False)
                        if job_empty_box is not None:
                            break
                        options_pages = chrome_cli.find_elements_d(by=By.CSS_SELECTOR, value='.options-pages a')
                        if len(options_pages) > 2:
                            n = int(options_pages[-2].get_attribute('innerText'))

                        # 解析内容
                        login_close()
                        jobs = chrome_cli.find_elements_d(by=By.CSS_SELECTOR, value='.job-list-box .job-card-wrapper')
                        for job in jobs:
                            append(f'###########{job.get_attribute('innerHTML')}')
                        break
                    except Exception as e:
                        append_e(str(e))
                        sleep(1)
                page += 1
                sleep(1)
            append(r=f'{city},{query}', f='jobs')


if __name__ == '__main__':
    chrome_cli = ChromeCli(headless=False)


    def login_close():
        close = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='[ka="boss-login-close"]', timeout=0, count=1, raise_e=False)
        if close is not None:
            chrome_cli.click(close)


    pull_cities()
    pull_queries()
    pull_jobs()
