from time import sleep

import undetected_chromedriver as uc

if __name__ == '__main__':
    driver = uc.Chrome()
    driver.maximize_window()
    driver.get('https://www.zhipin.com/web/geek/job')

    sleep(100)
