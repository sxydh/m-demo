# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy


class BossItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    pass


class JobItem(scrapy.Item):
    name = scrapy.Field()
    address = scrapy.Field()
    salary = scrapy.Field()
    company = scrapy.Field()
    city = scrapy.Field()
    industry = scrapy.Field()
    experience = scrapy.Field()
    degree = scrapy.Field()
    scale = scrapy.Field()
    job_tag = scrapy.Field()
    company_tag = scrapy.Field()
    body = scrapy.Field()
    job_id = scrapy.Field()
    job_url = scrapy.Field()
    job_list_url = scrapy.Field()
    remark = scrapy.Field()
