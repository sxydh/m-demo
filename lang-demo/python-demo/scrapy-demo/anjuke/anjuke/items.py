# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy


class AnjukeItem(scrapy.Item):
    name = scrapy.Field()
    address = scrapy.Field()
    type = scrapy.Field()
    tag = scrapy.Field()
    price = scrapy.Field()
    remark = scrapy.Field()
