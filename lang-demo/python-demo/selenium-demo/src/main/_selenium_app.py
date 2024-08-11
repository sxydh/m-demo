from time import sleep

from selenium import webdriver

# https://www.selenium.dev/documentation/webdriver/getting_started/first_script/
if __name__ == '__main__':
    driver = webdriver.Chrome()
    driver.get("https://www.zhcw.com/kjxx/ssq/")

    sleep(1000)
