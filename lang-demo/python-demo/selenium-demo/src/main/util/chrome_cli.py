from selenium import webdriver
from selenium.webdriver import ActionChains
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.support import expected_conditions as ec
from selenium.webdriver.support.wait import WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager


class ChromeCli:

    def __init__(self, url, proxy=None, headless=True, images_disabled=True):
        options = Options()
        if proxy:
            options.add_argument(f'--proxy-server={proxy}')
        if headless:
            options.add_argument('--headless=new')
        if images_disabled:
            options.add_argument('--blink-settings=imagesEnabled=false')
        self.driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=options
        )
        self.driver.maximize_window()
        self.driver.get(url)

    def find_element_d(self, by, value):
        return self.find_element(self.driver, by, value)

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

    def find_elements_d(self, by, value):
        return self.find_elements(self.driver, by, value)

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

    def click(self, ele):
        ActionChains(self.driver).move_to_element(ele).click().perform()
