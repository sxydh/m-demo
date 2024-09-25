import logging
import time

from selenium.webdriver.common.by import By

from src.main.util.cli import Cli
from src.main.util.common import get_sqlite_connection, read_rows


class QcwyApp:
    cli = None
    conn = None

    start_url = 'https://we.51job.com/pc/search?searchType=2&sortType=1'
    job_areas = ['090200']
    fun_types = []
    work_years = [('02', '1-3年'), ('03', '3-5年'), ('04', '5-10年'), ('05', '10年以上'), ('06', '无需经验')]
    degrees = [('01', '初中及以下'), ('02', '高中/中技/中专'), ('03', '大专'), ('04', '本科'), ('05', '硕士'), ('06', '博士'), ('07', '无')]
    company_sizes = [('01', '少于50人'), ('02', '50-150人'), ('03', '150-500人'), ('04', '500-1000人'), ('05', '1000-5000人'), ('06', '5000-10000人'), ('07', '10000人以上')]

    def __init__(self):
        self.init_search()
        self.init_db()
        self.init_cli()

    def init_search(self):
        self.fun_types = [f.split(',') for f in read_rows('fun_type.csv')]

    def init_db(self):
        self.conn = get_sqlite_connection('qcwy.db')
        self.conn.execute('create table if not exists qcwy_job(name, salary, address, company_name, company_size, fun_type, work_year, degree, job_tag, company_tag, remark)')

    def init_cli(self):
        self.cli = Cli(undetected=True,
                       images_disabled=True,
                       headless=False)

    def filter_url(self, url) -> bool:
        print(url)
        return True

    def start(self):
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
                            url += f'&timestamp={int(time.time())}'
                            seen = self.filter_url(url)

                            if not seen:
                                self.cli.get(url)
                                self.cli.find_element_d(by=By.CSS_SELECTOR, value='.joblist', raise_e=False)

    def close(self):
        self.cli.quit()
        self.conn.close()


class JobItem:
    name = None
    salary = None
    address = None
    company_name = None
    company_size = None
    fun_type = None
    work_year = None
    degree = None
    job_tag = None
    company_tag = None
    remark = None


if __name__ == '__main__':
    logging.basicConfig(level=logging.WARN)
    QcwyApp().start()
