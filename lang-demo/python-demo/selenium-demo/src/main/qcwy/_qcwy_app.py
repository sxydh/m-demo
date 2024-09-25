import json
import logging
import threading
import time
import uuid

from bs4 import BeautifulSoup
from selenium.webdriver.common.by import By

from src.main.util.cli import Cli
from src.main.util.common import get_sqlite_connection, read_rows


class JobItem:
    uid = None
    id = None
    name = None
    salary = None
    address = None
    company_name = None
    company_size = None
    fun_type = None
    work_year = None
    degree = None
    job_time = None
    job_tag = None
    job_url = None
    job_list_url = None
    job_page = None
    job_pages = None
    company_tag = None
    raw = None
    remark = None


class QcwyApp:
    run_flag = True

    cli = None
    conn = None

    start_url = 'https://we.51job.com/pc/search?searchType=2&sortType=1'
    job_areas = ['090200']
    fun_types = []
    work_years = [('02', '1-3年'), ('03', '3-5年'), ('04', '5-10年'), ('05', '10年以上'), ('06', '无需经验')]
    degrees = [('01', '初中及以下'), ('02', '高中/中技/中专'), ('03', '大专'), ('04', '本科'), ('05', '硕士'), ('06', '博士'), ('07', '无')]
    company_sizes = [('01', '少于50人'), ('02', '50-150人'), ('03', '150-500人'), ('04', '500-1000人'), ('05', '1000-5000人'), ('06', '5000-10000人'), ('07', '10000人以上')]

    def __init__(self):
        self.init_db()
        self.init_db_handler()
        self.init_console_handler()
        self.init_search()
        self.init_cli()

    def init_db(self):
        self.conn = get_sqlite_connection('qcwy.db')
        self.conn.execute('create table if not exists qcwy_job(uid text, id text, name text, salary text, address text, company_name text, company_size text, fun_type text, work_year text, degree text, job_time text, job_tag text, job_url text, job_list_url text, job_page text, job_pages text, company_tag text, raw text, remark text)')

    def init_db_handler(self):
        t = threading.Thread(target=self.db_handler)
        t.start()

    def init_console_handler(self):
        t = threading.Thread(target=self.console_handler)
        t.start()

    def init_search(self):
        self.fun_types = [f.split(',') for f in read_rows('fun_type.csv')]

    def init_cli(self):
        self.cli = Cli(undetected=True,
                       images_disabled=True,
                       headless=False)

    def filter_url(self, url) -> bool:
        if self.conn.execute('select 1 from qcwy_job where job_list_url = ?', [url]).fetchone():
            return True
        return False

    def run(self):
        for job_area in self.job_areas:
            for fun_type in self.fun_types:
                for work_year in self.work_years:
                    for degree in self.degrees:
                        for company_size in self.company_sizes:
                            url = self.start_url
                            url += f'&jobArea={job_area}'
                            url += f'&function={fun_type[0]}'
                            url += f'&workYear={work_year[0]}'
                            url += f'&degree={degree[0]}'
                            url += f'&companySize={company_size[0]}'

                            request_url = url
                            is_filtered = self.filter_url(request_url)
                            if is_filtered:
                                continue

                            self.cli.get(request_url)
                            page = 1
                            while True:
                                items = self.cli.find_elements_d(by=By.CSS_SELECTOR, value='.joblist-item,.j_nolist', timeout=1, count=5, raise_e=False)
                                pages = self.cli.find_elements_d(by=By.CSS_SELECTOR, value='.pageation .el-pager .number', timeout=0, count=1, raise_e=False)
                                pages = len(pages)
                                next_btn = self.cli.find_element_d(by=By.CSS_SELECTOR, value='.pageation .btn-next', timeout=0, count=1, raise_e=False)
                                verification = self.cli.find_element_d(by=By.CSS_SELECTOR, value='#nc_1_n1z', timeout=0, count=1, raise_e=False)
                                if verification:
                                    self.cli.click_and_move_by_x_offset(verification, 400)
                                    continue

                                self.parse_job_item(fun_type=fun_type[1],
                                                    work_year=work_year[1],
                                                    degree=degree[1],
                                                    company_size=company_size[1],
                                                    job_list_url=request_url,
                                                    pages=pages,
                                                    items=items)
                                if page < pages:
                                    self.cli.click(next_btn)
                                    page += 1
                                    continue
                                break

                            if not self.run_flag:
                                self.close()
                                return

    def close(self):
        self.cli.quit()
        self.conn.close()

    def parse_job_item(self, *, fun_type, work_year, degree, company_size, job_list_url, pages: int, items: list):
        for item in items:
            job_item = JobItem()
            job_item.fun_type = fun_type
            job_item.work_year = work_year
            job_item.degree = degree
            job_item.company_size = company_size
            job_item.job_list_url = job_list_url

            if 'joblist-item' in item.get_attribute('class'):
                job_item.job_pages = pages
                job_item.raw = item.get_attribute('innerHTML')
            self.save_job_item(job_item)

    def save_job_item(self, job_item: JobItem):
        self.conn.execute(f'insert into qcwy_job(uid, id, name, salary, address, company_name, company_size, fun_type, work_year, degree, job_time, job_tag, job_url, job_list_url, job_page, job_pages, company_tag, raw, remark) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                          [str(uuid.uuid4()), job_item.id, job_item.name, job_item.salary, job_item.address, job_item.company_name, job_item.company_size, job_item.fun_type, job_item.work_year, job_item.degree, job_item.job_time, job_item.job_tag, job_item.job_url, job_item.job_list_url, job_item.job_page, job_item.job_pages, job_item.company_tag, job_item.raw, job_item.remark])
        self.conn.commit()

    def db_handler(self):
        while self.run_flag:
            row = self.conn.execute('select uid, raw from qcwy_job where raw is not null and name is null limit 1').fetchone()
            if not row:
                time.sleep(1)
                continue

            uid = row[0]
            raw = row[1]

            job_item = JobItem()
            job_item.uid = uid
            soup = BeautifulSoup(raw, 'html.parser')
            sensors_data = soup.select_one('.sensors_exposure')['sensorsdata']
            sensors_data = json.loads(sensors_data)
            job_item.id = sensors_data.get('jobId')
            job_item.name = soup.select_one('.jname').text
            job_item.salary = soup.select_one('.sal').text
            job_item.address = sensors_data.get('jobArea')
            job_item.company_name = soup.select_one('.cname').text
            job_item.job_time = sensors_data.get('jobTime')
            job_item.job_tag = '###'.join([e.text for e in soup.select('.tags .tag')])
            job_item.job_page = sensors_data.get('pageNum')
            job_item.company_tag = '###'.join([e.text for e in soup.select('span.dc')])

            self.conn.execute(f'update qcwy_job set id=?, name=?, salary=?, address=?, company_name=?, job_time=?, job_tag=?, job_page=?, company_tag=? where uid=?',
                              [job_item.id, job_item.name, job_item.salary, job_item.address, job_item.company_name, job_item.job_time, job_item.job_tag, job_item.job_page, job_item.company_tag, job_item.uid])
            self.conn.commit()

    def console_handler(self):
        while True:
            cmd = input('Please input >>> ')
            if cmd == 'stop':
                logging.warning('>>> Ready to stop')
                self.run_flag = False
                break


if __name__ == '__main__':
    logging.basicConfig(level=logging.WARN)
    qcwy_app = QcwyApp()
    qcwy_app.run()
