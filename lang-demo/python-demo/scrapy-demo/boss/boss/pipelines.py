# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html
import os
import threading

from boss.util.common import get_sqlite_connection

# useful for handling different item types with a single interface

job_rlock = threading.RLock()


class BossPipeline:

    def __init__(self):
        self.conn = get_sqlite_connection()
        self.conn.execute("create table if not exists boss_job(name text, address text, salary text, company text, city text, industry text, experience text, degree text, scale text, job_tag text, company_tag text, body text, job_id text, job_url text, job_list_url text, remark text)")
        if os.environ.get("CLEAR_JOB") == "1":
            self.conn.execute("delete from boss_job where 1 = 1")
        self.conn.commit()

    def process_item(self, item, spider):
        with job_rlock:
            if self.conn.execute("select 1 from boss_job where job_id = ?", [item.get("job_id")]).fetchone():
                return
            self.conn.execute("insert into boss_job(name, address, salary, company, city, industry, experience, degree, scale, job_tag, company_tag, body, job_id, job_url, job_list_url, remark) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                              (item.get("name"), item.get("address"), item.get("salary"), item.get("company"), item.get("city"), item.get("industry"), item.get("experience"), item.get("degree"), item.get("scale"), item.get("job_tag"), item.get("company_tag"), item.get("body"), item.get("job_id"), item.get("job_url"), item.get("job_list_url"), item.get("remark")))
            self.conn.commit()

    def close_spider(self, spider):
        self.conn.close()
