from time import sleep
from urllib.parse import quote

from selenium.webdriver.common.by import By

from src.main.util.chrome_cli import ChromeCli
from src.main.util.common import append, append_e, read_rows


def pull_cities():
    pass


def pull_queries():
    pass


def pull_jobs():
    cities = read_rows('cities')
    queries = read_rows('queries')

    for city in cities:
        for query in queries:
            # 分页循环
            page = 1
            n = 1
            while page <= n:
                # 重试循环
                while True:
                    try:
                        chrome_cli.get(f'https://www.zhipin.com/web/geek/job?query={quote(query)}&city={quote(city)}&page={page}')
                        options_pages = chrome_cli.find_elements_d(by=By.CSS_SELECTOR, value='.options-pages a')
                        if len(options_pages) > 2:
                            n = int(options_pages[-2].get_attribute('innerText'))

                        # 解析内容
                        login_close()
                        jobs = chrome_cli.find_elements_d(by=By.CSS_SELECTOR, value='.job-list-box li')
                        for job in jobs:
                            append(job.get_attribute('innerHTML'))
                        break
                    except Exception as e:
                        append_e(str(e))
                        sleep(1)
                page += 1
                sleep(1)


if __name__ == '__main__':
    chrome_cli = ChromeCli(headless=False)


    def login_close():
        close = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='[ka="boss-login-close"]', timeout=0, count=1, raise_e=False)
        if close is not None:
            chrome_cli.click(close)


    pull_cities()
    pull_queries()
    pull_jobs()
