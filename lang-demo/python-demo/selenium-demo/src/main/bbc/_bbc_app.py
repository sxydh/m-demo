from selenium.webdriver.common.by import By

from src.main.util.cli import Cli
from src.main.util.common import append


def start():
    cli = Cli(proxy='127.0.0.1:10809')
    cli.get('https://www.bbc.com/news')

    while True:
        nav = cli.find_element_d(by=By.CSS_SELECTOR, value='[data-testid="level2-navigation-container"]')
        lis = cli.find_elements(src=nav, by=By.CSS_SELECTOR, value='li')
        for (i, li) in enumerate(lis):
            nav = cli.find_element_d(by=By.CSS_SELECTOR, value='[data-testid="level2-navigation-container"]')
            lis = cli.find_elements(src=nav, by=By.CSS_SELECTOR, value='li')
            append(lis[i].get_attribute('innerText'))
            cli.click(lis[i])
            while True:
                grid = cli.find_element_d(by=By.CSS_SELECTOR, value='[data-testid="alaska-grid"]')
                links = cli.find_elements(src=grid, by=By.CSS_SELECTOR, value='[data-testid="internal-link"]')
                for link in links:
                    headline = cli.find_element(src=link, by=By.CSS_SELECTOR, value='[data-testid="card-headline"]')
                    description = cli.find_element(src=link, by=By.CSS_SELECTOR, value='[data-testid="card-description"]')
                    append(f'    {headline.get_attribute('innerText')}')
                    append(f'        {description.get_attribute('innerText')}')
                next_button = cli.find_element_d(by=By.CSS_SELECTOR, value='[data-testid="pagination-next-button"]')
                if next_button.get_attribute('disabled') == 'true':
                    break
                cli.click(next_button)


if __name__ == '__main__':
    start()
