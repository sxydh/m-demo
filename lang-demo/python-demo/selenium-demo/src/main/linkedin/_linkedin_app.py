import random
from time import sleep

from selenium.webdriver.common.by import By

from src.main.util.cli import Cli
from src.main.util.common import append_e, read_rows


def pull_jobs():
    locations = read_rows('locations')
    keywords = read_rows('keywords')

    for location in locations:
        for keyword in keywords:

            # 分页循环
            page_num = 1
            n = 10
            while page_num <= n:

                # 重试循环
                while True:
                    try:
                        chrome_cli.get(f'https://www.linkedin.com/')
                        if in_login():
                            continue
                        menu_jobs = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='[data-tracking-control-name="guest_homepage-basic_guest_nav_menu_jobs"]')
                        chrome_cli.click(menu_jobs)

                        job_list = chrome_cli.find_elements_d(by=By.CSS_SELECTOR, value='.jobs-search__results-list li', timeout=1, count=3)
                        for job in job_list:
                            sleep(random.choice([1, 1, 2, 2, 3]))
                            chrome_cli.move_to_element(job)
                            chrome_cli.click(job)
                            show_more = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='[data-tracking-control-name="public_jobs_show-more-html-btn"]')
                            sleep(random.choice([1, 1, 2, 2, 3]))
                            chrome_cli.click(show_more)
                        break
                    except Exception as e:
                        append_e(str(e))
                        sleep(1)

                page_num += 1
                sleep(1)


if __name__ == '__main__':
    chrome_cli = Cli(undetected=True, headless=False, proxy='127.0.0.1:10809')


    def in_login():
        login_form = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='.login__form_action_container', timeout=0, count=1, raise_e=False)
        if login_form is not None:
            return True
        return False


    pull_jobs()
