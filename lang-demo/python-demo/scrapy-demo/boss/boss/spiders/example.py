import os
import sys
from typing import Any

import scrapy
from scrapy.cmdline import execute
from scrapy.http import Response


class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = []
    start_urls = ["https://zhipin.com"]

    def parse(self, response: Response, **kwargs: Any) -> Any:
        pass


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "WARN"])
