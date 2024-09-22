import os
import sys
from typing import Any

import scrapy
from scrapy.cmdline import execute
from scrapy.http import Response

from tjbz.items import TjbzItem


# noinspection DuplicatedCode
class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = ["stats.gov.cn"]
    start_urls = ["https://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2023/index.html"]
    max_level = 4

    def parse(self, response: Response, **kwargs: Any) -> Any:
        meta = response.meta
        meta_parent = meta.get("meta_parent", TjbzItem())
        parent_code = meta_parent.get("code", None)

        trs = response.css(".provincetr td")
        level = 1
        if len(trs) == 0 and self.max_level > 1:
            trs += response.css(".citytr")
            level = 2
        if len(trs) == 0 and self.max_level > 2:
            trs += response.css(".countytr")
            level = 3
        if len(trs) == 0 and self.max_level > 3:
            trs += response.css(".towntr")
            level = 4

        if len(trs) == 0:
            if "url" in meta_parent:
                yield scrapy.Request(url=meta_parent["url"], callback=self.parse, meta={"meta_parent": meta_parent}, dont_filter=True)
                return

        for tr in trs:
            tds = tr.css("td")
            item = TjbzItem()
            if len(tds) == 1:
                al = tds[0].css("a")
                href = al[0].css("::attr(href)").get()
                item["code"] = href.replace('.html', '')
                item["name"] = tds[0].css("::text").get().strip()
                item["url"] = response.urljoin(href)
                item["level"] = level
            else:
                item["code"] = tds[0].css("::text").get().strip()
                item["name"] = tds[-1].css("::text").get().strip()
                item["parent_code"] = parent_code
                al = tds[0].css("a")
                if len(al) > 0:
                    item["url"] = response.urljoin(al[0].css("::attr(href)").get())
                item["level"] = level
            yield item

            if "url" in item and item["level"] < self.max_level:
                yield scrapy.Request(url=item["url"], callback=self.parse, meta={"meta_parent": item})


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "DEBUG"])
