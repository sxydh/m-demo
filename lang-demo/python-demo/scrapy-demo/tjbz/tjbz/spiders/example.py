import os
import scrapy
import sys
from scrapy.cmdline import execute
from scrapy.http import Response
from typing import Any

from tjbz.items import TjbzItem


class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = ["stats.gov.cn"]
    start_urls = ["https://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2023/index.html"]

    def parse(self, response: Response, **kwargs: Any) -> Any:
        provinces = response.css(".provincetr a")
        yield self.parse_do(response, provinces, self.parse_city)

    def parse_city(self, response: Response):
        cities = response.css(".citytr")
        self.parse_do(response, cities, self.parse_county)

    def parse_county(self, response: Response):
        counties = response.css(".countytr")
        self.parse_do(response, counties, self.parse_town)

    def parse_town(self, response: Response):
        towns = response.css(".towntr")
        self.parse_do(response, towns, None)

    def parse_do(self, response: Response, arr, callback):
        for ele in arr:
            alist = ele.css("a")
            item = TjbzItem()
            item.code = alist[0].css("::text").get()
            item.name = alist[-1].css("::text").get()
            item.url = response.urljoin(alist[-1].css("::attr(href)").get())
            item.parent_code = response.meta["meta_parent"].code
            yield item
            if item.url:
                yield scrapy.Request(url=item.url, callback=callback, meta={"meta_parent": item})


if __name__ == '__main__':
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(['scrapy', 'crawl', 'example'])
