import datetime
from time import sleep

from selenium import webdriver
from selenium.webdriver.common.by import By


def add_days(ds, delta):
    d = datetime.datetime.strptime(ds, '%Y-%m-%d')
    return (d + datetime.timedelta(days=delta)).strftime('%Y-%m-%d')


# https://www.selenium.dev/documentation/webdriver/getting_started/first_script/
if __name__ == '__main__':
    driver = webdriver.Chrome()

    driver.get("https://www.zhcw.com/kjxx/ssq/")

    sds = '2004-01-01'
    eds = ''
    while eds <= '2024-01-01':
        eds = add_days(sds, 10)

        # 自定义查询
        driver.find_element(by=By.CLASS_NAME, value='wq-xlk01').click()
        cx_tj = driver.find_element(by=By.CLASS_NAME, value='cx-tj')
        cx_tj.find_elements(by=By.TAG_NAME, value='div')[2].click()

        # 按日期查询
        start_c = driver.find_element(by=By.ID, value='startC')
        end_c = driver.find_element(by=By.ID, value='endC')
        start_c.clear()
        end_c.clear()
        start_c.send_keys(sds)
        end_c.send_keys(eds)

        # 开始查询
        driver.find_elements(by=By.CLASS_NAME, value='JG-an03')[2].click()

        sds = eds

        sleep(2)
