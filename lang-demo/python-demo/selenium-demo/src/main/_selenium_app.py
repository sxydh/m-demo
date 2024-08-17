from time import sleep

from selenium import webdriver

if __name__ == '__main__':
    driver = webdriver.Chrome()

    driver.get("https://www.zhcw.com/kjxx/ssq/")

    sleep(2)
