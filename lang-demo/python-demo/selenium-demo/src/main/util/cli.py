import undetected_chromedriver as uc
from selenium import webdriver
from selenium.webdriver import ActionChains
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.support import expected_conditions as ec
from selenium.webdriver.support.wait import WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager

from src.main.util.common import append_e


class Cli:

    def __init__(self,
                 undetected=False,
                 proxy=None,
                 headless=True,
                 images_disabled=True,
                 argument=None):
        options = Options()
        if argument is not None:
            for arg in argument:
                options.add_argument(arg)
        if undetected:
            if proxy:
                options.add_argument(f'--proxy-server={proxy}')
            if images_disabled:
                options.add_argument('--blink-settings=imagesEnabled=false')
            self.driver = uc.Chrome(
                headless=headless,
                options=options
            )
            self.driver.maximize_window()
        else:
            if proxy:
                options.add_argument(f'--proxy-server={proxy}')
            if headless:
                options.add_argument('--headless=new')
            if images_disabled:
                options.add_argument('--blink-settings=imagesEnabled=false')
            self.driver = webdriver.Chrome(
                options=options
            )
            self.driver.maximize_window()

    def get(self, url):
        self.driver.get(url)

    def find_element_d(self, by, value, timeout=10, count=3, raise_e=True):
        return self.find_element(src=self.driver, by=by, value=value, timeout=timeout, count=count, raise_e=raise_e)

    def find_element(self, src, by, value, timeout=10, count=3, raise_e=True):
        arr = self.find_elements(src=src, by=by, value=value, timeout=timeout, count=count, raise_e=raise_e)
        if len(arr) != 0:
            return arr[0]
        return None

    def find_elements_d(self, by, value, timeout=10, count=3, raise_e=True):
        return self.find_elements(src=self.driver, by=by, value=value, timeout=timeout, count=count, raise_e=raise_e)

    def find_elements(self, src, by, value, timeout=10, count=3, raise_e=True):
        while count > 0:
            try:
                WebDriverWait(self.driver, timeout).until(ec.presence_of_element_located((by, value)))
                return src.find_elements(by=by, value=value)
            except Exception as e:
                count -= 1
                append_e(str(e))
        if raise_e:
            raise Exception(f'by={by}, value={value} not found')
        return []

    def move_to_element(self, ele):
        ActionChains(self.driver).move_to_element(ele).perform()

    def click(self, ele):
        ActionChains(self.driver).move_to_element(ele).click().perform()

    def close(self):
        self.driver.close()
