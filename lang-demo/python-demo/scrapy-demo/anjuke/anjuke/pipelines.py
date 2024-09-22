# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html
from anjuke.items import CityItem, NewHouseItem
from util.common import get_sqlite_connection


# useful for handling different item types with a single interface


class AnjukePipeline:
    def __init__(self):
        self.conn = None

    def open_spider(self, spider):
        self.conn = get_sqlite_connection()
        self.conn.execute("create table if not exists anjuke_city(province text, name text, url text, new_house_url text, new_house_total text, new_house_total_num integer, body text, remark text)")
        self.conn.execute("create table if not exists anjuke_new_house(province text, city text, name text, address text, type text, tag text, price text, price_num integer, url text, body text, remark text)")
        self.conn.execute("delete from anjuke_city where 1 = 1")
        self.conn.execute("delete from anjuke_new_house where 1 = 1")
        self.conn.commit()

    def process_item(self, item, spider):
        if isinstance(item, CityItem):
            self.conn.execute("insert into anjuke_city(province, name, url, new_house_url, new_house_total, new_house_total_num, body, remark) values(?, ?, ?, ?, ?, ?, ?, ?)",
                              (item.get("province"), item.get("name"), item.get("url"), item.get("new_house_url"), item.get("new_house_total"), item.get("new_house_total_num"), item.get("body"), item.get("remark")))
            self.conn.commit()
        elif isinstance(item, NewHouseItem):
            self.conn.execute("insert into anjuke_new_house(province, city, name, address, type, tag, price, price_num, url, body, remark) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                              (item.get("province"), item.get("city"), item.get("name"), item.get("address"), item.get("type"), item.get("tag"), item.get("price"), item.get("price_num"), item.get("url"), item.get("body"), item.get("remark")))
            self.conn.commit()

    def close_spider(self, spider):
        self.conn.close()
