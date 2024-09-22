# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html
from util.common import get_sqlite_connection


# useful for handling different item types with a single interface


class TjbzPipeline:

    def __init__(self):
        self.conn = None

    def open_spider(self, spider):
        self.conn = get_sqlite_connection()
        self.conn.execute("create table if not exists tjbz(code text, name text, parent_code text, url text, remark text)")

    def process_item(self, item, spider):
        self.conn.execute("insert into tjbz(code, name, parent_code, url, remark) values (?, ?, ?, ?, ?)",
                          (item["code"], item["name"], item.get("parent_code", ""), item["url"], item.get("remark", "")))

    def close_spider(self, spider):
        self.conn.close()
