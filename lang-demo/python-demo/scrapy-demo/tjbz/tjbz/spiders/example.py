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

    def parse(self, response: Response, **kwargs: Any) -> Any:
        trs = response.css(".provincetr td")
        trs += response.css(".citytr")
        trs += response.css(".countytr")
        trs += response.css(".towntr")
        for tr in trs:
            tds = tr.css("td")
            item = TjbzItem()
            if len(tds) == 1:
                item["name"] = tds[0].css("::text").get().strip()
                al = tds[0].css("a")
                if len(al) > 0:
                    href = al[0].css("::attr(href)").get()
                    item["code"] = href.replace('.html', '')
                    item["url"] = response.urljoin(href)
            else:
                item["code"] = tds[0].css("::text").get().strip()
                item["name"] = tds[-1].css("::text").get().strip()
                item["parent_code"] = response.meta["meta_parent"]["code"]
                al = tds[0].css("a")
                if len(al) > 0:
                    item["url"] = response.urljoin(al[0].css("::attr(href)").get())
            yield item
            if "url" in item:
                yield scrapy.Request(url=item["url"], callback=self.parse, meta={"meta_parent": item})


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "WARN"])
