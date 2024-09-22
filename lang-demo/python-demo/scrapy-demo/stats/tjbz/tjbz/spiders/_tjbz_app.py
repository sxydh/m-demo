from typing import Any

import scrapy
from scrapy.http import Response

from stats.tjbz.tjbz.items import TjbzItem


class TjbzApp(scrapy.Spider):
    name = "tjbz"
    allowed_domains = []
    start_urls = ["https://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2023/index.html"]

    def parse(self, response: Response, **kwargs: Any) -> Any:
        provinces = response.css(".provincetr a")
        yield self.parse_do(provinces, response)

    def parse_city(self, response: Response):
        cities = response.css(".citytr")
        self.parse_do(cities, response)

    def parse_county(self, response: Response):
        counties = response.css(".countytr")
        self.parse_do(counties, response)

    def parse_do(self, arr, response: Response):
        for ele in arr:
            alist = ele.css("a")
            item = TjbzItem()
            item.code = alist[0].css("::text").get()
            item.name = alist[-1].css("::text").get()
            item.url = response.urljoin(alist[-1].css("::attr(href)").get())
            item.parent_code = response.meta["meta_parent"].code
            yield item
            yield scrapy.Request(url=item.url, callback=self.parse_county, meta={"meta_parent": item})
