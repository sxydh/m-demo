import os
import re
import sys
from typing import Any

import scrapy
from scrapy.cmdline import execute
from scrapy.http import Response

from anjuke.items import CityItem, NewHouseItem


class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = ["anjuke.com"]
    start_urls = ["https://www.anjuke.com/sy-city.html?from=HomePage_City"]

    def parse(self, response: Response, **kwargs: Any) -> Any:
        if "callback" in response.url:
            yield scrapy.Request(self.start_urls[0], callback=self.parse, dont_filter=True)
            return

        cities = response.css(".ajk-city-cell.is-letter li a")
        for city in cities:
            city_item = CityItem()
            city_item["name"] = city.css("::text").get().strip()
            city_item["url"] = response.urljoin(city.css("::attr(href)").get())
            yield scrapy.Request(city_item["url"], callback=self.parse_city_new_house_url, meta={"meta_city_item": city_item})

    def parse_city_new_house_url(self, response: Response) -> Any:
        meta_city_item = response.meta["meta_city_item"]
        url = meta_city_item["url"]

        if "callback" in response.url:
            yield scrapy.Request(url, callback=self.parse, meta={"meta_city_item": meta_city_item}, dont_filter=True)
            return

        navs = response.css(".nav-channel-list > li:first-child")
        if len(navs) == 0:
            meta_city_item["remark"] = "len(response.css(\".nav-channel-list > li:first-child\")) == 0"
            yield meta_city_item
            return
        nav = navs[0]
        nav_text = nav.css("::text").get().strip()
        if nav_text != "新房":
            meta_city_item["remark"] = "nav.css(\"::text\").get().strip() != \"新房\""
            yield meta_city_item
            return
        new_house_url = response.urljoin(nav.css("a::attr(href)").get())
        new_house_url = new_house_url.split("?")[0]
        new_house_url = f"{new_house_url}loupan/all/s1"
        meta_city_item["new_house_url"] = new_house_url
        yield scrapy.Request(new_house_url, callback=self.parse_new_house_list, meta={"meta_city_item": meta_city_item})

    def parse_new_house_list(self, response: Response) -> Any:
        meta_city_item = response.meta["meta_city_item"]
        new_house_url = meta_city_item["new_house_url"]

        if "callback" in response.url:
            yield scrapy.Request(new_house_url, callback=self.parse_new_house_list, meta={"meta_city_item": meta_city_item}, dont_filter=True)
            return

        totals = response.css(".list-results .result")
        if len(totals) != 0:
            total = totals[0]
            meta_city_item["new_house_total"] = self.parse_text_helper(total, "*")
        yield meta_city_item

        new_house_list = response.css(".list-results .item-mod")
        for new_house in new_house_list:
            new_house_item = NewHouseItem()
            new_house_item["city"] = meta_city_item["name"]
            new_house_item["name"] = self.parse_text_helper(new_house, ".lp-name")
            new_house_item["address"] = self.parse_text_helper(new_house, ".address")
            new_house_item["type"] = self.parse_text_helper(new_house, ".huxing")
            new_house_item["tag"] = self.parse_text_helper(new_house, ".tag-panel i,span", is_multi=True)
            price = self.parse_text_helper(new_house, ".price")
            new_house_item["price"] = price
            if price is not None and len(price) > 0:
                print(price)
                new_house_item["price_num"] = re.search(r'(\d+)', price).group(1)
            new_house_item["url"] = new_house.css("a.lp-name::attr(href)").get()
            yield new_house_item

    def parse_text_helper(self, src, selector, is_multi=False):
        arr = src.css(selector)
        if not is_multi:
            ele = arr[0] if len(arr) > 0 else None
            text = "".join([se.strip() for se in ele.css("::text").getall()]) if ele else None
        else:
            text = ", ".join([ele.css("::text").get().strip() for ele in arr])
        return text.replace("\xa0", "") if text else None


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "DEBUG"])
