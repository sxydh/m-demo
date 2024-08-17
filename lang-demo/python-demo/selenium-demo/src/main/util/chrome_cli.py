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

    def find_element_d(self, by, value, count=3):
        return self.find_element(src=self.driver, by=by, value=value, count=count)

    def find_element(self, src, by, value, count=3):
        while count > 0:
            try:
                WebDriverWait(self.driver, 10).until(ec.presence_of_element_located((by, value)))
                return src.find_element(by=by, value=value)
            except Exception as e:
                count -= 1
                print(e)
        raise Exception(f'by={by}, value={value} not found')

    def find_elements_d(self, by, value, count=3):
        return self.find_elements(src=self.driver, by=by, value=value, count=count)

    def find_elements(self, src, by, value, count=3):
        while count > 0:
            try:
                WebDriverWait(self.driver, 10).until(ec.presence_of_element_located((by, value)))
                return src.find_elements(by=by, value=value)
            except Exception as e:
                count -= 1
                print(e)
        raise Exception(f'by={by}, value={value} not found')

    def click(self, ele):
        ActionChains(self.driver).move_to_element(ele).click().perform()
