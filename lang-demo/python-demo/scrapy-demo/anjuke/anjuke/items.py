# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy


class AnjukeItem(scrapy.Item):
    pass


class CityItem(scrapy.Item):
    province = scrapy.Field()
    name = scrapy.Field()
    url = scrapy.Field()
    new_house_url = scrapy.Field()
    new_house_total = scrapy.Field()
    remark = scrapy.Field()


class NewHouseItem(scrapy.Item):
    province = scrapy.Field()
    city = scrapy.Field()
    name = scrapy.Field()
    address = scrapy.Field()
    type = scrapy.Field()
    tag = scrapy.Field()
    price = scrapy.Field()
    price_num = scrapy.Field()
    url = scrapy.Field()
    remark = scrapy.Field()