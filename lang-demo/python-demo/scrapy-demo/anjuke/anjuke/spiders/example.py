import copy
import logging
import os
import re
import sys
import threading
import uuid
from typing import Any

import scrapy
from scrapy.cmdline import execute
from scrapy.http import Response

from anjuke.items import CityItem, NewHouseItem

city_rlock = threading.RLock()
city_handled = False


# noinspection DuplicatedCode
class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = []
    start_urls = ["https://www.anjuke.com/sy-city.html?from=HomePage_City"]

    def parse(self, response: Response, **kwargs: Any) -> Any:
        if self.is_need_request_again(self.start_urls[0], response):
            yield scrapy.Request(self.start_urls[0], callback=self.parse, dont_filter=True)
            return

        global city_rlock
        global city_handled
        with city_rlock:
            if city_handled:
                return
            city_handled = True

        cities = response.css(".ajk-city-cell.is-letter li a")
        for city in cities:
            city_item = CityItem()
            city_item["uid"] = str(uuid.uuid4())
            city_item["name"] = city.css("::text").get().strip()
            city_item["url"] = response.urljoin(city.css("::attr(href)").get())
            yield city_item
            yield scrapy.Request(city_item["url"], callback=self.parse_city_new_house_url, meta={"meta_city_item": copy.copy(city_item)})

    def parse_city_new_house_url(self, response: Response) -> Any:
        meta_city_item = response.meta["meta_city_item"]
        url = meta_city_item["url"]

        if self.is_need_request_again(url, response):
            yield scrapy.Request(url, callback=self.parse_city_new_house_url, meta={"meta_city_item": copy.copy(meta_city_item)}, dont_filter=True)
            return

        navs = response.css(".nav-channel-list > li:first-child")
        if len(navs) == 0:
            meta_city_item["body"] = response.body
            meta_city_item["remark"] = "len(response.css(\".nav-channel-list > li:first-child\")) == 0"
            yield meta_city_item
            return
        nav = navs[0]
        nav_text = nav.css("::text").get().strip()
        if nav_text != "新房":
            meta_city_item["body"] = response.body
            meta_city_item["remark"] = f"nav.css(\"::text\").get().strip() == \"{nav_text}\""
            yield meta_city_item
            return
        new_house_url = response.urljoin(nav.css("a::attr(href)").get())
        new_house_url = new_house_url.split("?")[0]

        new_house_url = f"{new_house_url}loupan/all/s1"
        meta_city_item["new_house_url"] = new_house_url
        yield meta_city_item
        yield scrapy.Request(new_house_url, callback=self.parse_new_house_list, meta={"meta_city_item": copy.copy(meta_city_item)})

    def parse_new_house_list(self, response: Response) -> Any:
        meta_city_item = response.meta["meta_city_item"]
        new_house_url = meta_city_item["new_house_url"]
        city_name = meta_city_item["name"]

        if self.is_need_request_again(new_house_url, response):
            meta_city_item["body"] = response.body
            meta_city_item["remark"] = "is_need_request_again"
            yield meta_city_item
            yield scrapy.Request(new_house_url, callback=self.parse_new_house_list, meta={"meta_city_item": copy.copy(meta_city_item)}, dont_filter=True)
            return

        page_city_name = self.parse_text_helper(response, ".sel-city .city", replaces=[" "])
        if page_city_name is None or (page_city_name != city_name and not (page_city_name in city_name or city_name in page_city_name)):
            logging.warning(f"### city_name from page ### {page_city_name} <=> {city_name}")
            meta_city_item["body"] = response.body
            meta_city_item["remark"] = f"page_city_name={page_city_name} <=> city_name={city_name}"
            yield meta_city_item
            return

        totals = response.css(".list-results .result")
        if len(totals) != 0:
            total = totals[0]
            total = self.parse_text_helper(total, "*", replaces=[" "])
            meta_city_item["new_house_total"] = total
            if total is not None and len(total) > 0:
                meta_city_item["new_house_total_num"] = re.search(r'(\d+)', total).group(1)
        yield meta_city_item

        new_house_list = response.css(".list-results .item-mod")
        if len(new_house_list) == 0:
            meta_city_item["body"] = response.body
            meta_city_item["remark"] = "len(new_house_list) == 0"
            yield meta_city_item
            return
        for new_house in new_house_list:
            new_house_item = NewHouseItem()
            new_house_item["city"] = meta_city_item["name"]
            new_house_item["city_new_house_url"] = new_house_url
            new_house_item["name"] = self.parse_text_helper(new_house, ".lp-name", replaces=[" "])
            new_house_item["address"] = self.parse_text_helper(new_house, ".address", replaces=[" "])
            new_house_item["type"] = self.parse_text_helper(new_house, ".huxing", replaces=[" ", "\n"])
            new_house_item["tag"] = self.parse_text_helper(new_house, ".tag-panel i,span", is_multi=True)
            price = self.parse_text_helper(new_house, ".price,.around-price", replaces=[" "])
            new_house_item["price"] = price
            if price is not None and len(price) > 0:
                new_house_item["price_num"] = re.search(r'(\d+)', price).group(1)
            new_house_item["url"] = new_house.css("a.lp-name::attr(href)").get()
            new_house_item["body"] = str(new_house)
            yield new_house_item

    def parse_text_helper(self, src, selector, is_multi=False, replaces: list = None):
        ret = None
        arr = src.css(selector)
        if len(arr) == 0:
            return ret
        if not is_multi:
            ele = arr[0]
            texts = ele.css("::text").getall()
            if len(texts) > 0:
                ret = "".join(texts)
        else:
            ret = []
            for ele in arr:
                text = ele.css("::text").get()
                if text:
                    ret.append(text.strip())
            ret = "###".join(ret)
        if ret:
            ret = ret.replace("\xa0", "").strip()
            if replaces:
                for replace in replaces:
                    ret = ret.replace(replace, "")
        return ret

    def is_need_request_again(self, target_url: str, response: Response = None) -> bool:
        # 页面是反爬验证
        antibot = response.css("#\\@\\@xxzlGatewayUrl")
        antibot_url = antibot[0].css("::text").get().strip() if antibot is not None and len(antibot) > 0 else None
        if antibot_url is not None:
            logging.warning(f"### antibot from page ### {antibot_url} <=> {target_url}")
            return True
        # 页面没有内容
        body = response.css("body")
        if len(body) == 0:
            logging.warning(f"### empty page ### len(body) == 0 <=> {target_url}")
            return True
        # 地址是反爬验证
        if "callback" in response.url:
            logging.warning(f"### antibot from url ### {response.url} <=> {target_url}")
            return True
        # 其它错误码
        if response.status != 200:
            logging.warning(f"### error code ### {response.status} <=> {target_url}")
            return True
        return False


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "WARN"])
