# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html
import threading
import uuid

from m_pyutil.msqlite import create, save, select_one

from anjuke.items import CityItem, NewHouseItem

# useful for handling different item types with a single interface

DB_FILE = "anjuke.db"
CITY_RLOCK = threading.RLock()


class AnjukePipeline:

    def open_spider(self, spider):
        create(sql="create table if not exists anjuke_city(id integer primary key autoincrement, uid text, province text, name text, url text, new_house_url text, new_house_total text, new_house_total_num integer, body text, remark text)",
               f=DB_FILE)
        create(sql="create table if not exists anjuke_new_house(id integer primary key autoincrement, uid text, province text, city text, city_new_house_url text, name text, address text, location text, type text, tag text, price text, price_num integer, url text, body text, remark text)",
               f=DB_FILE)
        save(sql="delete from anjuke_city where 1 = 1",
             f=DB_FILE)
        save(sql="delete from anjuke_new_house where 1 = 1",
             f=DB_FILE)

    def process_item(self, item, spider):
        if isinstance(item, CityItem):
            with CITY_RLOCK:
                db_item = select_one(sql="select * from anjuke_city where uid = ?",
                                     params=[item.get("uid")],
                                     f=DB_FILE)
                if db_item is None:
                    save(sql="insert into anjuke_city(uid, province, name, url, new_house_url, new_house_total, new_house_total_num, body, remark) values(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                         params=[item.get("uid"), item.get("province"), item.get("name"), item.get("url"), item.get("new_house_url"), item.get("new_house_total"), item.get("new_house_total_num"), item.get("body"), item.get("remark")],
                         f=DB_FILE)
                else:
                    save(sql="update anjuke_city set province = ?, name = ?, url = ?, new_house_url = ?, new_house_total = ?, new_house_total_num = ?, body = ?, remark = ? where uid = ?",
                         params=[item.get("province"), item.get("name"), item.get("url"), item.get("new_house_url"), item.get("new_house_total"), item.get("new_house_total_num"), item.get("body"), item.get("remark"), item.get("uid")],
                         f=DB_FILE)
        elif isinstance(item, NewHouseItem):
            save(sql="insert into anjuke_new_house(uid, province, city, city_new_house_url, name, address, type, tag, price, price_num, url, body, remark) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                 params=[str(uuid.uuid4()), item.get("province"), item.get("city"), item.get("city_new_house_url"), item.get("name"), item.get("address"), item.get("type"), item.get("tag"), item.get("price"), item.get("price_num"), item.get("url"), item.get("body"), item.get("remark")],
                 f=DB_FILE)

    def close_spider(self, spider):
        pass
