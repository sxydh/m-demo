import logging
import random
import threading
import time

from m_pyutil import mdate
from m_pyutil.msqlite import create, save
from m_pyutil.selenium.mchrome import UcChromeCli

DB_FILE = 'ssq.db'


class SsqApp(threading.Thread):

    def __init__(self):
        create(sql=f'create table if not exists t_ssq (id integer primary key autoincrement, d text, r text, r2 text, r3 text, r4 text, r5 text, r6 text, b text, n text)',
               f=DB_FILE)
        save(sql=f'delete from t_ssq where 1 = 1',
             f=DB_FILE)
        self.cli = UcChromeCli()
        super().__init__()

    def run(self):
        self.cli.get('https://www.zhcw.com/kjxx/ssq/')

        max_eds = mdate.nowd()
        sds = mdate.add_days(max_eds, -90)
        flag = True
        while flag:
            eds = mdate.add_days(sds, 20)
            if eds >= max_eds:
                eds = max_eds
                flag = False

            # 点开自定义查询
            wq_xlk01 = self.cli.query_element_d('.wq-xlk01')
            self.cli.click(wq_xlk01)
            cx_tj = self.cli.query_element_d(value='.cx-tj')
            div_2 = self.cli.query_elements(src=cx_tj, value='div')[2]
            self.cli.click(div_2)

            # 点开按日期查询
            start_c = self.cli.query_element_d('#startC')
            end_c = self.cli.query_element_d('#endC')
            start_c.clear()
            end_c.clear()
            start_c.send_keys(sds)
            end_c.send_keys(eds)

            # 点击开始查询
            time.sleep(random.choice(range(1, 2)))
            jg_an03_2 = self.cli.query_elements_d('.JG-an03')[2]
            self.cli.click(jg_an03_2)
            time.sleep(0.5)

            # 解析页面
            while True:
                try:
                    tbody = self.cli.query_element_d('tbody')
                    trs = self.cli.query_elements(src=tbody, value='tr')
                    for tr in trs:
                        params = []
                        tds = self.cli.query_elements(src=tr, value='td')
                        params.append(tds[0].get_attribute('innerText').strip())
                        spans = self.cli.query_elements(src=tds[2], value='span')
                        for span in spans:
                            params.append(span.get_attribute('innerText').strip())
                        params.append(tds[3].get_attribute('innerText').strip())
                        params.append(tds[5].get_attribute('innerText').strip())

                        save(sql='insert into t_ssq(d, r, r2, r3, r4, r5, r6, b, n) values(?, ?, ?, ?, ?, ? ,?, ? ,?)',
                             params=params,
                             f=DB_FILE)
                    break
                except Exception as e:
                    logging.error(e)

            sds = eds


if __name__ == '__main__':
    SsqApp().start()
