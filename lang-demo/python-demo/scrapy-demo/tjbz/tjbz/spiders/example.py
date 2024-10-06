import logging
import os
import sys
from typing import Any

import scrapy
from scrapy.cmdline import execute
from scrapy.http import Response

from tjbz.items import TjbzItem


class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = ["stats.gov.cn"]
    start_urls = ["https://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2023/index.html"]
    max_level = 4

    def parse(self, response: Response, **kwargs: Any) -> Any:
        meta: dict = response.meta
        meta_parent = meta.get("meta_parent", TjbzItem())
        parent_code = meta_parent.get("code")

        trs = response.css(".provincetr td")
        level = 1
        if not trs and self.max_level > 1:
            trs = response.css(".citytr")
            level = 2
        if not trs and self.max_level > 2:
            trs = response.css(".countytr")
            level = 3
        if not trs and self.max_level > 3:
            trs = response.css(".towntr")
            level = 4

        # 页面异常重试
        if not trs and meta_parent.get("url"):
            logging.warning(f"none data page: meta_parent.url={meta_parent.get("url")} <=> response.url={response.url}")
            yield scrapy.Request(url=meta_parent.get("url"), callback=self.parse, meta={"meta_parent": meta_parent}, dont_filter=True)
            return

        for tr in trs:
            tds = tr.css("td")
            item = TjbzItem()
            try:
                if level == 1:
                    alist = tds[0].css("a")
                    href = alist[0].css("::attr(href)").get()
                    item["code"] = href.replace(".html", "")
                    item["name"] = tds[0].css("::text").get().strip()
                    item["url"] = response.urljoin(href)
                    item["level"] = level
                else:
                    item["code"] = tds[0].css("::text").get().strip()
                    item["name"] = tds[-1].css("::text").get().strip()
                    item["parent_code"] = parent_code
                    item["level"] = level
                    alist = tds[0].css("a")
                    if len(alist) > 0:
                        item["url"] = response.urljoin(alist[0].css("::attr(href)").get())
                yield item
            except Exception as e:
                logging.warning(f"parse error: {str(e)}")

            if item.get("url") and level < self.max_level:
                yield scrapy.Request(url=item.get("url"), callback=self.parse, meta={"meta_parent": item})


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "WARN"])
