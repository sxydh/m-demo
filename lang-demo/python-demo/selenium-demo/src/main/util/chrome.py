from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.support import expected_conditions as ec
from selenium.webdriver.support.wait import WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager


class ChromeCli:

    def __init__(self, url, proxy=None):
        options = Options()
        options.add_argument('--headless=new')
        options.add_argument('--blink-settings=imagesEnabled=false')
        if proxy:
            options.add_argument(f'--proxy-server={proxy}')
        self.driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=options
        )
        self.driver.maximize_window()
        self.driver.get(url)

    def find_element(self, src, by, value):
        i = 3
        while i > 0:
            try:
                WebDriverWait(self.driver, 10).until(ec.presence_of_element_located((by, value)))
                return src.find_element(by=by, value=value)
            except Exception as e:
                i -= 1
                print(e)
        raise Exception(f'by={by}, value={value} not found')

    def find_elements(self, src, by, value):
        c = 3
        while c > 0:
            try:
                WebDriverWait(self.driver, 10).until(ec.presence_of_element_located((by, value)))
                return src.find_elements(by=by, value=value)
            except Exception as e:
                c -= 1
                print(e)
        raise Exception(f'by={by}, value={value} not found')
