from selenium import webdriver
from selenium.webdriver import ActionChains
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as ec
from selenium.webdriver.support.wait import WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager


def find_element(src, by, value):
    i = 3
    while i > 0:
        try:
            WebDriverWait(driver, 10).until(ec.presence_of_element_located((by, value)))
            return src.find_element(by=by, value=value)
        except Exception as e:
            i -= 1
            print(e)
    raise Exception(f'by={by}, value={value} not found')


def find_elements(src, by, value):
    i = 3
    while i > 0:
        try:
            WebDriverWait(driver, 10).until(ec.presence_of_element_located((by, value)))
            return src.find_elements(by=by, value=value)
        except Exception as e:
            i -= 1
            print(e)
    raise Exception(f'by={by}, value={value} not found')


def write(r):
    with open('tmp/output.txt', mode='a', encoding='utf-8') as o:
        o.write(r + '\r')


if __name__ == '__main__':
    options = Options()
    options.add_argument('--proxy-server=127.0.0.1:10809')
    options.add_argument('--headless=new')
    options.add_argument('--blink-settings=imagesEnabled=false')
    driver = webdriver.Chrome(
        service=Service(ChromeDriverManager().install()),
        options=options
    )
    driver.maximize_window()

    driver.get('https://www.bbc.com/news')

    while True:
        nav = find_element(src=driver, by=By.CSS_SELECTOR, value='[data-testid="level2-navigation-container"]')
        lis = find_elements(src=nav, by=By.CSS_SELECTOR, value='li')
        for (i, li) in enumerate(lis):
            nav = find_element(src=driver, by=By.CSS_SELECTOR, value='[data-testid="level2-navigation-container"]')
            lis = find_elements(src=nav, by=By.CSS_SELECTOR, value='li')
            write(lis[i].get_attribute('innerText'))
            ActionChains(driver).move_to_element(lis[i]).click().perform()
            while True:
                grid = find_element(src=driver, by=By.CSS_SELECTOR, value='[data-testid="alaska-grid"]')
                links = find_elements(src=grid, by=By.CSS_SELECTOR, value='[data-testid="internal-link"]')
                for link in links:
                    headline = find_element(src=link, by=By.CSS_SELECTOR, value='[data-testid="card-headline"]')
                    description = find_element(src=link, by=By.CSS_SELECTOR, value='[data-testid="card-description"]')
                    write('    ' + headline.get_attribute('innerText'))
                    write('        ' + description.get_attribute('innerText'))
                nextButton = find_element(src=driver, by=By.CSS_SELECTOR, value='[data-testid="pagination-next-button"]')
                if nextButton.get_attribute('disabled') == 'true':
                    break
                ActionChains(driver).move_to_element(nextButton).click().perform()
