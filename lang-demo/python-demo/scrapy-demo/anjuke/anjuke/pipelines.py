# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html
import threading
import uuid

from m_pyutil.msqlite import create, save, select_one

from anjuke.items import CityItem, NewHouseItem
from util.common import get_sqlite_connection

# useful for handling different item types with a single interface

city_rlock = threading.RLock()


class AnjukePipeline:
    db_file = "anjuke.db"

    def __init__(self):
        self.conn = None

    def open_spider(self, spider):
        self.conn = get_sqlite_connection()
        create(sql="create table if not exists anjuke_city(id integer primary key autoincrement, uid text, province text, name text, url text, new_house_url text, new_house_total text, new_house_total_num integer, body text, remark text)",
               f=self.db_file)
        create(sql="create table if not exists anjuke_new_house(id integer primary key autoincrement, uid text, province text, city text, city_new_house_url text, name text, address text, type text, tag text, price text, price_num integer, url text, body text, remark text)",
               f=self.db_file)
        save(sql="delete from anjuke_city where 1 = 1",
             f=self.db_file)
        save(sql="delete from anjuke_new_house where 1 = 1",
             f=self.db_file)

    def process_item(self, item, spider):
        if isinstance(item, CityItem):
            with city_rlock:
                db_item = select_one(sql="select * from anjuke_city where uid = ?",
                                     params=[item.get("uid")],
                                     f=self.db_file)
                if db_item is None:
                    save(sql="insert into anjuke_city(uid, province, name, url, new_house_url, new_house_total, new_house_total_num, body, remark) values(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                         params=[item.get("uid"), item.get("province"), item.get("name"), item.get("url"), item.get("new_house_url"), item.get("new_house_total"), item.get("new_house_total_num"), item.get("body"), item.get("remark")],
                         f=self.db_file)
                else:
                    save(sql="update anjuke_city set province = ?, name = ?, url = ?, new_house_url = ?, new_house_total = ?, new_house_total_num = ?, body = ?, remark = ? where uid = ?",
                         params=[item.get("province"), item.get("name"), item.get("url"), item.get("new_house_url"), item.get("new_house_total"), item.get("new_house_total_num"), item.get("body"), item.get("remark"), item.get("uid")],
                         f=self.db_file)
        elif isinstance(item, NewHouseItem):
            save(sql="insert into anjuke_new_house(uid, province, city, city_new_house_url, name, address, type, tag, price, price_num, url, body, remark) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                 params=[str(uuid.uuid4()), item.get("province"), item.get("city"), item.get("city_new_house_url"), item.get("name"), item.get("address"), item.get("type"), item.get("tag"), item.get("price"), item.get("price_num"), item.get("url"), item.get("body"), item.get("remark")],
                 f=self.db_file)

    def close_spider(self, spider):
        pass
