# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy


class TjbzItem(scrapy.Item):
    code = scrapy.Field()
    name = scrapy.Field()
    parent_code = scrapy.Field()
    url = scrapy.Field()
