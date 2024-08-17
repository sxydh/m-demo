from selenium.webdriver.common.by import By

from src.main.util.chrome_cli import ChromeCli
from src.main.util.common import append

if __name__ == '__main__':
    chrome_cli = ChromeCli(proxy='127.0.0.1:10809')

    while True:
        nav = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='[data-testid="level2-navigation-container"]')
        lis = chrome_cli.find_elements(src=nav, by=By.CSS_SELECTOR, value='li')
        for (i, li) in enumerate(lis):
            nav = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='[data-testid="level2-navigation-container"]')
            lis = chrome_cli.find_elements(src=nav, by=By.CSS_SELECTOR, value='li')
            append(lis[i].get_attribute('innerText'))
            chrome_cli.click(lis[i])
            while True:
                grid = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='[data-testid="alaska-grid"]')
                links = chrome_cli.find_elements(src=grid, by=By.CSS_SELECTOR, value='[data-testid="internal-link"]')
                for link in links:
                    headline = chrome_cli.find_element(src=link, by=By.CSS_SELECTOR, value='[data-testid="card-headline"]')
                    description = chrome_cli.find_element(src=link, by=By.CSS_SELECTOR, value='[data-testid="card-description"]')
                    append('    ' + headline.get_attribute('innerText'))
                    append('        ' + description.get_attribute('innerText'))
                next_button = chrome_cli.find_element_d(by=By.CSS_SELECTOR, value='[data-testid="pagination-next-button"]')
                if next_button.get_attribute('disabled') == 'true':
                    break
                chrome_cli.click(next_button)
