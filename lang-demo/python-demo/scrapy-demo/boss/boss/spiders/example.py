import os
import sys
from typing import Any

import scrapy
from scrapy.cmdline import execute
from scrapy.http import Response


class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = []
    start_urls = ["https://www.zhipin.com/web/geek/job?city=101270100"]

    industries = []

    def __init__(self, **kwargs: Any):
        super().__init__(**kwargs)
        # 数据来自 https://www.zhipin.com/wapi/zpCommon/data/industryFilterExemption
        with open("industry.csv", "r") as f:
            for line in f.readlines():
                self.industries.append(line.strip().split(","))

    def parse(self, response: Response, **kwargs: Any) -> Any:
        pass


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "WARN"])
