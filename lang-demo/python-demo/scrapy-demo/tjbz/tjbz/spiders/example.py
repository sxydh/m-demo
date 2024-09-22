import os
import sys
import uuid
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
    level = 4

    def parse(self, response: Response, **kwargs: Any) -> Any:
        meta = response.meta
        meta_parent = meta.get("meta_parent", TjbzItem())
        parent_code = meta_parent.get("code", None)
        parent_level = meta_parent.get("level", 0)

        trs = response.css(".provincetr td")
        if len(trs) == 0 and self.level > 1:
            trs += response.css(".citytr")
        if len(trs) == 0 and self.level > 2:
            trs += response.css(".countytr")
        if len(trs) == 0 and self.level > 3:
            trs += response.css(".towntr")

        if len(trs) == 0:
            url = response.url
            uid = str(uuid.uuid4())
            if "?" in response.url:
                url += f"&uid={uid}"
            else:
                url += f"?uid={uid}"
            return scrapy.Request(url=url, callback=self.parse, meta={"meta_parent": meta_parent})

        for tr in trs:
            tds = tr.css("td")
            item = TjbzItem()
            if len(tds) == 1:
                al = tds[0].css("a")
                href = al[0].css("::attr(href)").get()
                item["code"] = href.replace('.html', '')
                item["name"] = tds[0].css("::text").get().strip()
                item["url"] = response.urljoin(href)
                item["level"] = parent_level + 1
            else:
                item["code"] = tds[0].css("::text").get().strip()
                item["name"] = tds[-1].css("::text").get().strip()
                item["parent_code"] = parent_code
                al = tds[0].css("a")
                if len(al) > 0:
                    item["url"] = response.urljoin(al[0].css("::attr(href)").get())
                item["level"] = parent_level + 1
            yield item

            if "url" in item and item["level"] < self.level:
                yield scrapy.Request(url=item["url"], callback=self.parse, meta={"meta_parent": item})


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "DEBUG"])
