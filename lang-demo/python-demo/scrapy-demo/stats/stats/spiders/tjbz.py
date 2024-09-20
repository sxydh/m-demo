from typing import Any
from urllib.parse import urljoin

import scrapy
from scrapy.http import Response

from util.common import append


# noinspection PyMethodMayBeStatic
class TjbzSpider(scrapy.Spider):
    name = "tjbz"
    allowed_domains = ["www.stats.gov.cn"]
    start_urls = ["https://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2023/index.html"]

    def parse(self, response: Response, **kwargs: Any):
        provinces = response.css(".provincetr a")
        for province in provinces:
            append(f='province', r=', '.join(province.css("::text").getall()))
            yield scrapy.Request(urljoin(response.url, province.css("::attr(href)").get()), callback=self.parse_city)

    def parse_city(self, response: Response):
        cities = response.css(".citytr")
        for city in cities:
            city = city.css('a')
            if city:
                city = city[0]
                append(f='city', r=', '.join(city.css("::text").getall()))
                yield scrapy.Request(urljoin(response.url, city.css("::attr(href)").get()), callback=self.parse_county)

    def parse_county(self, response: Response):
        counties = response.css(".countytr")
        for county in counties:
            county = county.css('a')
            if county:
                county = county[0]
                append(f='county', r=', '.join(county.css("::text").getall()))
                yield scrapy.Request(urljoin(response.url, county.css("::attr(href)").get()), callback=self.parse_town)

    def parse_town(self, response: Response):
        towns = response.css(".towntr")
        for town in towns:
            town = town.css('a')
            if town:
                town = town[0]
                append(f='town', r=', '.join(town.css("::text").getall()))
