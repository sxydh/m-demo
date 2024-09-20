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
            append(f='province', r=province.css("::text").get())
            yield scrapy.Request(urljoin(response.url, province.css("::attr(href)").get()), callback=self.parse_city)

    def parse_city(self, response: Response):
        append(f='city_status', r=f'{response.status}, {response.url}')
        cities = response.css(".citytr td:last-of-type a[href]")
        for city in cities:
            append(f='city', r=city.css("::text").get())
            yield scrapy.Request(urljoin(response.url, city.css("::attr(href)").get()), callback=self.parse_county)

    def parse_county(self, response: Response):
        append(f='county_status', r=f'{response.status}, {response.url}')
        counties = response.css(".countytr td:last-of-type a[href]")
        for county in counties:
            append(f='county', r=county.css("::text").get())
            yield scrapy.Request(urljoin(response.url, county.css("::attr(href)").get()), callback=self.parse_town)

    def parse_town(self, response: Response):
        append(f='town_status', r=f'{response.status}, {response.url}')
        towns = response.css(".towntr td:last-of-type a[href]")
        for town in towns:
            append(f='town', r=town.css("::text").get())
