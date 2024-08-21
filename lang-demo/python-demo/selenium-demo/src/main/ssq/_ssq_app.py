from time import sleep

from selenium.webdriver.common.by import By

from src.main.util.cli import Cli
from src.main.util.common import add_days, append_e, get_sqlite_connection


def start():
    cli = Cli(headless=False, images_disabled=True)
    cli.get('https://www.zhcw.com/kjxx/ssq/')

    sds = '2024-08-01'
    eds = ''
    while eds <= '2024-08-01':
        eds = add_days(sds, 20)

        # 自定义查询
        wq_xlk01 = cli.find_element_d(by=By.CLASS_NAME, value='wq-xlk01')
        cli.click(wq_xlk01)
        cx_tj = cli.find_element_d(by=By.CLASS_NAME, value='cx-tj')
        div_2 = cli.find_elements(src=cx_tj, by=By.TAG_NAME, value='div')[2]
        cli.click(div_2)

        # 按日期查询
        start_c = cli.find_element_d(by=By.ID, value='startC')
        end_c = cli.find_element_d(by=By.ID, value='endC')
        start_c.clear()
        end_c.clear()
        start_c.send_keys(sds)
        end_c.send_keys(eds)

        # 开始查询
        jg_an03_2 = cli.find_elements_d(by=By.CLASS_NAME, value='JG-an03')[2]
        cli.click(jg_an03_2)

        # 解析结果
        while True:
            try:
                sleep(0.5)
                tbody = cli.find_element_d(by=By.TAG_NAME, value='tbody')
                trs = cli.find_elements(src=tbody, by=By.TAG_NAME, value='tr')
                for tr in trs:
                    tds = cli.find_elements(src=tr, by=By.TAG_NAME, value='td')
                    r = f'\'{tds[0].get_attribute('innerText')}\', '
                    spans = cli.find_elements(src=tds[2], by=By.TAG_NAME, value='span')
                    for span in spans:
                        r += f'\'{span.get_attribute('innerText')}\', '
                    r += f'\'{tds[3].get_attribute('innerText')}\''

                    with get_sqlite_connection() as conn:
                        conn.cursor()
                        conn.execute(f'insert into t_ssq values ({r})')

                break
            except Exception as e:
                append_e(str(e))

        sds = eds

        sleep(2)


def init():
    with get_sqlite_connection() as conn:
        conn.cursor()
        conn.execute(f'create table if not exists t_ssq (id, n1, n2, n3, n4, n5, n6, n7)')


if __name__ == '__main__':
    start()
