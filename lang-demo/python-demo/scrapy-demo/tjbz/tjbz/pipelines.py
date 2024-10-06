# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html
from m_pyutil.msqlite import create, save

# useful for handling different item types with a single interface


DB_FILE = "tjbz.db"


class TjbzPipeline:

    def open_spider(self, spider):
        create(sql="create table if not exists tjbz(id integer primary key autoincrement, code text, name text, parent_code text, url text, level, remark text)",
               f=DB_FILE)
        save(sql="delete from tjbz where 1 = 1",
             f=DB_FILE)

    def process_item(self, item, spider):
        save(sql="insert into tjbz(code, name, parent_code, url, level, remark) values (?, ?, ?, ?, ?, ?)",
             params=[item.get("code"), item.get("name"), item.get("parent_code"), item.get("url"), item.get("level"), item.get("remark")],
             f=DB_FILE)

    def close_spider(self, spider):
        pass
