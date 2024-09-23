# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html


# useful for handling different item types with a single interface

from boss.util.common import get_sqlite_connection


class BossPipeline:

    def __init__(self):
        self.conn = get_sqlite_connection()

    def process_item(self, item, spider):
        print(item)
