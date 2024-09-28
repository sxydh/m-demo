import json
import logging
import threading
import time
import uuid

from bs4 import BeautifulSoup
from m_pyutil.mhttp import Server, MyHTTPRequestHandler
from selenium.webdriver.common.by import By

from src.main.util.cli import Cli
from src.main.util.common import get_sqlite_connection, read_rows, try_save_sqlite, select_one_sqlite


class JobItem:
    id = None
    uid = None
    name = None
    salary = None
    address = None
    company_name = None
    company_size = None
    fun_type = None
    work_year = None
    degree = None
    job_id = None
    job_time = None
    job_tag = None
    job_url = None
    job_list_url = None
    job_page = None
    job_pages = None
    company_tag = None
    raw = None
    remark = None


class QcwyApp(threading.Thread):
    run_flag = True
    db_file = 'qcwy.db'
    cli = None

    start_url = 'https://we.51job.com/pc/search?searchType=2&sortType=1'
    job_areas = ['090200']
    fun_types = []
    work_years = [('02', '1-3年'), ('03', '3-5年'), ('04', '5-10年'), ('05', '10年以上'), ('06', '无需经验')]
    degrees = [('01', '初中及以下'), ('02', '高中/中技/中专'), ('03', '大专'), ('04', '本科'), ('05', '硕士'), ('06', '博士'), ('07', '无')]
    company_sizes = [('01', '少于50人'), ('02', '50-150人'), ('03', '150-500人'), ('04', '500-1000人'), ('05', '1000-5000人'), ('06', '5000-10000人'), ('07', '10000人以上')]

    def __init__(self, group=None, target=None, name=None, args=(), kwargs=None, *, daemon=None):
        super().__init__(group, target, name, args, kwargs, daemon=daemon)
        self.init_cli()
        self.init_db()
        self.init_db_handler()
        self.init_console_handler()
        self.init_queue()
        self.init_server()

    def init_cli(self):
        self.cli = Cli(undetected=True,
                       images_disabled=True,
                       headless=False)

    def init_db(self):
        with get_sqlite_connection(self.db_file) as conn:
            conn.execute('create table if not exists qcwy_queue(id integer primary key autoincrement, uid text unique, job_area text, fun_type text, work_year text, degree text, company_size text, uid_owner text)')
            conn.execute('create table if not exists qcwy_job(id integer primary key autoincrement, uid text, name text, salary text, address text, company_name text, company_size text, fun_type text, work_year text, degree text, job_id text, job_time text, job_tag text, job_url text, job_list_url text, job_page text, job_pages text, company_tag text, raw text, remark text)')

    def init_db_handler(self):
        t = threading.Thread(target=self.db_handler)
        t.start()

    def init_console_handler(self):
        t = threading.Thread(target=self.console_handler)
        t.start()

    def init_queue(self):
        t = threading.Thread(target=self.init_queue_handler)
        t.start()

    def init_queue_handler(self):
        self.fun_types = [f.split(',') for f in read_rows('fun_type.csv')]
        for job_area in self.job_areas:
            for fun_type in self.fun_types:
                for work_year in self.work_years:
                    for degree in self.degrees:
                        for company_size in self.company_sizes:
                            if not self.run_flag:
                                return

                            url = self.start_url
                            url += f'&jobArea={job_area}'
                            url += f'&function={fun_type[0]}'
                            url += f'&workYear={work_year[0]}'
                            url += f'&degree={degree[0]}'
                            url += f'&companySize={company_size[0]}'

                            time.sleep(1)

                            exists = select_one_sqlite(self.db_file,
                                                       'select 1 from qcwy_queue where uid = ?',
                                                       [url])
                            if exists:
                                continue
                            try_save_sqlite(self.db_file,
                                            'insert into qcwy_queue(uid, job_area, fun_type, work_year, degree, company_size) values(?, ?, ?, ?, ?, ?)',
                                            [url, job_area, fun_type[1], work_year[1], degree[1], company_size[1]])

    def init_server(self):
        t = threading.Thread(target=self.init_server_handler())
        t.start()

    def init_server_handler(self):
        def post_handler(handler: MyHTTPRequestHandler):
            content_length = int(handler.headers['Content-Length'])
            post_data = handler.rfile.read(content_length)
            post_data = json.loads(post_data)
            print(post_data)
            handler.send_response(200)
            handler.end_headers()

        Server(post_handler=post_handler).start()

    def run(self):
        while self.run_flag:
            uid_owner = str(uuid.uuid4())
            updated = try_save_sqlite(self.db_file,
                                      'update qcwy_queue set uid_owner = ? where id = (select t2.id from qcwy_queue t2 where t2.uid_owner is null limit 1)',
                                      [uid_owner])
            if updated == 0:
                time.sleep(1)
                continue

            popped = select_one_sqlite(self.db_file,
                                       'select uid, job_area, fun_type, work_year, degree, company_size from qcwy_queue where uid_owner = ?',
                                       [uid_owner])
            url = popped[0]
            fun_type = popped[2]
            work_year = popped[3]
            degree = popped[4]
            company_size = popped[5]

            self.cli.get(url)
            page = 1
            while True:
                items = self.cli.find_elements_d(by=By.CSS_SELECTOR, value='.joblist-item,.j_nolist', timeout=1, count=5, raise_e=False)
                pages = self.cli.find_elements_d(by=By.CSS_SELECTOR, value='.pageation .el-pager .number', timeout=0, count=1, raise_e=False)
                pages = int(pages[-1].get_attribute('innerText').strip()) if len(pages) > 0 else 0
                next_btn = self.cli.find_element_d(by=By.CSS_SELECTOR, value='.pageation .btn-next', timeout=0, count=1, raise_e=False)
                verification = self.cli.find_element_d(by=By.CSS_SELECTOR, value='#nc_1_n1z', timeout=0, count=1, raise_e=False)
                if verification:
                    self.cli.click_and_move_by_x_offset(verification, 400)
                    continue
                if len(items) == 0:
                    self.cli.get(url)
                    page = 1
                    continue

                self.parse_job_item(fun_type=fun_type,
                                    work_year=work_year,
                                    degree=degree,
                                    company_size=company_size,
                                    job_list_url=url,
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

    def parse_job_item(self, *, fun_type, work_year, degree, company_size, job_list_url, pages: int, items: list):
        for item in items:
            job_item = JobItem()
            job_item.fun_type = fun_type
            job_item.work_year = work_year
            job_item.degree = degree
            job_item.company_size = company_size
            job_item.job_list_url = job_list_url
            job_item.job_pages = pages

            if 'joblist-item' in item.get_attribute('class'):
                job_item.raw = item.get_attribute('innerHTML')
            self.save_job_item(job_item)

    def save_job_item(self, job_item: JobItem):
        try_save_sqlite(self.db_file,
                        'insert into qcwy_job(uid, name, salary, address, company_name, company_size, fun_type, work_year, degree, job_id, job_time, job_tag, job_url, job_list_url, job_page, job_pages, company_tag, raw, remark) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                        [str(uuid.uuid4()), job_item.name, job_item.salary, job_item.address, job_item.company_name, job_item.company_size, job_item.fun_type, job_item.work_year, job_item.degree, job_item.job_id, job_item.job_time, job_item.job_tag, job_item.job_url, job_item.job_list_url, job_item.job_page, job_item.job_pages, job_item.company_tag, job_item.raw, job_item.remark])

    def parse_text_helper(self, src, selector, is_multi=False, sep='###'):
        elements = src.select(selector)
        if len(elements) == 0:
            return None
        if not is_multi:
            return elements[0].text.strip()
        return sep.join([e.text.strip() for e in elements])

    def db_handler(self):
        while self.run_flag:
            row = select_one_sqlite(self.db_file,
                                    'select uid, raw from qcwy_job where raw is not null and name is null limit 1')
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
            job_item.name = self.parse_text_helper(soup, '.jname')
            job_item.salary = self.parse_text_helper(soup, '.sal')
            job_item.address = sensors_data.get('jobArea')
            job_item.company_name = self.parse_text_helper(soup, '.cname')
            job_item.job_id = sensors_data.get('jobId')
            job_item.job_time = sensors_data.get('jobTime')
            job_item.job_tag = self.parse_text_helper(soup, '.tags .tag', is_multi=True)
            job_item.job_page = sensors_data.get('pageNum')
            job_item.company_tag = self.parse_text_helper(soup, 'span.dc', is_multi=True)

            try_save_sqlite(self.db_file,
                            'update qcwy_job set name=?, salary=?, address=?, company_name=?, job_id=?, job_time=?, job_tag=?, job_page=?, company_tag=? where uid=?',
                            [job_item.name, job_item.salary, job_item.address, job_item.company_name, job_item.job_id, job_item.job_time, job_item.job_tag, job_item.job_page, job_item.company_tag, job_item.uid])

    def console_handler(self):
        while True:
            cmd = input('Please input >>> ')
            if cmd == 'stop':
                logging.warning(f'>>> Ready to stop {threading.get_ident()}')
                self.run_flag = False
                break


if __name__ == '__main__':
    logging.basicConfig(level=logging.WARN)
    for _ in range(5):
        time.sleep(2)
        QcwyApp().start()
