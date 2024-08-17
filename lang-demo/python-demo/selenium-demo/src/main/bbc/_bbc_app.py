from selenium.webdriver import ActionChains
from selenium.webdriver.common.by import By

from src.main.util.chrome import ChromeCli
from src.main.util.common import append

if __name__ == '__main__':
    chromeCli = ChromeCli(url='https://www.bbc.com/news', proxy='127.0.0.1:10809')
    driver = chromeCli.driver

    while True:
        nav = chromeCli.find_element(src=driver, by=By.CSS_SELECTOR, value='[data-testid="level2-navigation-container"]')
        lis = chromeCli.find_elements(src=nav, by=By.CSS_SELECTOR, value='li')
        for (i, li) in enumerate(lis):
            nav = chromeCli.find_element(src=driver, by=By.CSS_SELECTOR, value='[data-testid="level2-navigation-container"]')
            lis = chromeCli.find_elements(src=nav, by=By.CSS_SELECTOR, value='li')
            append(lis[i].get_attribute('innerText'))
            ActionChains(driver).move_to_element(lis[i]).click().perform()
            while True:
                grid = chromeCli.find_element(src=driver, by=By.CSS_SELECTOR, value='[data-testid="alaska-grid"]')
                links = chromeCli.find_elements(src=grid, by=By.CSS_SELECTOR, value='[data-testid="internal-link"]')
                for link in links:
                    headline = chromeCli.find_element(src=link, by=By.CSS_SELECTOR, value='[data-testid="card-headline"]')
                    description = chromeCli.find_element(src=link, by=By.CSS_SELECTOR, value='[data-testid="card-description"]')
                    append('    ' + headline.get_attribute('innerText'))
                    append('        ' + description.get_attribute('innerText'))
                nextButton = chromeCli.find_element(src=driver, by=By.CSS_SELECTOR, value='[data-testid="pagination-next-button"]')
                if nextButton.get_attribute('disabled') == 'true':
                    break
                ActionChains(driver).move_to_element(nextButton).click().perform()
