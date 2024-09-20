from typing import Any
from urllib.parse import urljoin

import scrapy
from scrapy.http import Response


# noinspection PyMethodMayBeStatic
class TjbzSpider(scrapy.Spider):
    name = "tjbz"
    allowed_domains = ["www.stats.gov.cn"]
    start_urls = ["https://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2023/index.html"]

    def parse(self, response: Response, **kwargs: Any):
        provinces = response.css(".provincetr a")
        for province in provinces:
            print(province.css("::text").getall())
            yield scrapy.Request(urljoin(response.url, province.css("::attr(href)").get()), callback=self.parse_city)

    def parse_city(self, response: Response):
        cities = response.css(".citytr a")
        for city in cities:
            print(city.css("::text").getall())
            yield scrapy.Request(urljoin(response.url, city.css("::attr(href)").get()), callback=self.parse_county)

    def parse_county(self, response: Response):
        counties = response.css(".countytr a")
        for county in counties:
            print(county.css("::text").getall())
            yield scrapy.Request(urljoin(response.url, county.css("::attr(href)").get()), callback=self.parse_town)

    def parse_town(self, response: Response):
        towns = response.css(".towntr a")
        for town in towns:
            print(town.css("::text").getall())
