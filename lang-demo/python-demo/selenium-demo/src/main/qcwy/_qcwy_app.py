import json
import logging
import os
import socket
import threading
import time
import uuid
from json import JSONDecodeError
from sqlite3 import IntegrityError
from urllib.parse import urlparse, parse_qs

from m_pyutil.mhttp import Server, MyHTTPRequestHandler
from m_pyutil.msqlite import create, save, select_one
from m_pyutil.mtmp import read_rows
from selenium.webdriver.common.by import By

import definitions
from src.main.util.cli import Cli


class JobItem:
    id = None
    uid = None
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
        self.init_console_handler()
        self.init_queue()
        self.init_extension_server()

    def init_cli(self):
        self.cli = Cli(undetected=True,
                       images_disabled=True,
                       headless=False,
                       unpacked_extensions=[f'{definitions.ROOT_DIR}/doc/united-extension'])

    def init_db(self):
        create(sql='create table if not exists qcwy_queue(id integer primary key autoincrement, uid text not null unique, job_area text, fun_type text, work_year text, degree text, company_size text, uid_owner text)',
               f=self.db_file)
        create(sql='create table if not exists qcwy_job(id integer primary key autoincrement, uid text not null unique, queue_uid text, raw text, remark text)',
               f=self.db_file)

    def init_console_handler(self):
        t = threading.Thread(target=self.console_handler)
        t.start()

    def console_handler(self):
        while True:
            cmd = input('Please input >>> ')
            if cmd == 'stop':
                logging.warning(f'>>> Ready to stop {self.name}')
                self.run_flag = False
                return

    def init_queue(self):
        t = threading.Thread(target=self.queue_handler)
        t.start()

    def queue_handler(self):
        self.fun_types = [f.split(',') for f in read_rows('fun_type.csv')]
        for job_area in self.job_areas:
            for fun_type in self.fun_types:
                for work_year in self.work_years:
                    for degree in self.degrees:
                        for company_size in self.company_sizes:
                            if not self.run_flag:
                                return

                            url = self.build_url(job_area, fun_type[0], work_year[0], degree[0], company_size[0])
                            try:
                                save(sql='insert into qcwy_queue(uid, job_area, fun_type, work_year, degree, company_size) values(?, ?, ?, ?, ?, ?)',
                                     params=[url, job_area, fun_type[1], work_year[1], degree[1], company_size[1]],
                                     f=self.db_file)
                            except IntegrityError as _:
                                continue
                            time.sleep(1)

    def build_url(self, job_area: str, fun_type: str, work_year: str, degree: str, company_size: str) -> str:
        url = self.start_url
        url += f'&jobArea={job_area}'
        url += f'&function={fun_type}'
        url += f'&workYear={work_year}'
        url += f'&degree={degree}'
        url += f'&companySize={company_size}'
        return url

    def init_extension_server(self):
        t = threading.Thread(target=self.extension_server_handler)
        t.start()

    def extension_server_handler(self):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            result = s.connect_ex(('127.0.0.1', 8080))
            if result == 0:
                logging.warning(f'{self.name}: port 8080 is already in use')
                return

        Server(post_handler=self.post_handler).start()

    def post_handler(self, handler: MyHTTPRequestHandler):
        path = handler.path
        parse_path = urlparse(path)
        path_query_params = parse_qs(parse_path.query)

        res_body = {}
        param_url = path_query_params.get('url')
        if not param_url or len(param_url) == 0:
            logging.warning(f'param url is empty: {path}')
        else:
            param_url = param_url[0]
            if 'proxy_config' in param_url:
                res_body = self.post_handle_proxy_config()
            elif 'https://we.51job.com/api/job/search-pc' in param_url and '&function=' in param_url:
                res_body = self.post_handle_job(param_url, handler)

        handler.send_response(200)
        handler.send_header('Content-type', 'application/json')
        handler.end_headers()
        handler.wfile.write(json.dumps(res_body).encode('utf-8'))

    def post_handle_proxy_config(self) -> dict:
        return {
            'host': os.environ.get('KDL_HOST'),
            'port': int(os.environ.get('KDL_PORT')),
            'username': os.environ.get('KDL_USERNAME'),
            'password': os.environ.get('KDL_PASSWORD')
        }

    def post_handle_job(self, url: str, handler: MyHTTPRequestHandler) -> dict:
        parse_url = urlparse(url)
        url_query_params = parse_qs(parse_url.query)
        content_length = int(handler.headers['Content-Length'])
        raw = handler.rfile.read(content_length).decode('utf-8')
        try:
            json.loads(raw)
        except JSONDecodeError as _:
            return {}

        job_area = url_query_params.get('jobArea')[0]
        fun_type = url_query_params.get('function')[0]
        work_year = url_query_params.get('workYear')[0]
        degree = url_query_params.get('degree')[0]
        company_size = url_query_params.get('companySize')[0]
        page_num = url_query_params.get('pageNum')[0]
        url = self.build_url(job_area, fun_type, work_year, degree, company_size)
        try:
            save(sql='insert into qcwy_job(uid, queue_uid, raw) select ?, t.uid, ? from qcwy_queue t where t.uid = ?',
                 params=[f'{url}&pageNum={page_num}', raw, url],
                 f=self.db_file)
        except IntegrityError as _:
            logging.warning(f'job queue_uid already exists: {url}')
        return {}

    def run(self):
        while True:
            if not self.run_flag:
                self.close()
                return

            uid_owner = str(uuid.uuid4())
            updated = save(sql='update qcwy_queue set uid_owner = ? where id = (select t.id from qcwy_queue t where t.uid_owner is null limit 1)',
                           params=[uid_owner],
                           f=self.db_file)
            if updated == 0:
                time.sleep(1)
                continue

            popped = select_one(sql='select uid from qcwy_queue where uid_owner = ?',
                                params=[uid_owner],
                                f=self.db_file)
            url = popped[0]

            time.sleep(1)
            self.cli.get(url)
            page = 1
            retry = 0
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
                    retry += 1
                    if retry > 60:
                        self.run_flag = False
                        break
                    continue

                if page < pages:
                    time.sleep(1)
                    self.cli.click(next_btn)
                    page += 1
                    continue
                break

    def close(self):
        self.cli.quit()


if __name__ == '__main__':
    logging.basicConfig(level=logging.WARN)
    for idx, _ in enumerate(range(5)):
        time.sleep(2)
        QcwyApp(name=f'qcwy_app_{idx}').start()
