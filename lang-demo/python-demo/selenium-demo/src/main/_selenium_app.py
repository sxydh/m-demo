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
    while eds <= '2024-08-01':
        eds = add_days(sds, 20)

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

        # 解析结果
        r = ''
        while True:
            try:
                sleep(1)
                tbody = driver.find_element(by=By.TAG_NAME, value='tbody')
                trs = tbody.find_elements(by=By.TAG_NAME, value='tr')
                for tr in trs:
                    tds = tr.find_elements(by=By.TAG_NAME, value='td')
                    r += tds[0].get_attribute('innerText') + ', '
                    spans = tds[2].find_elements(by=By.TAG_NAME, value='span')
                    for span in spans:
                        r += span.get_attribute('innerText') + ', '
                    r += tds[3].get_attribute('innerText') + '\r'
                break
            except Exception as e:
                print(e)
        with open('tmp/output.txt', mode='a', encoding='utf-8') as o:
            o.write(r)

        sds = eds

        sleep(2)
