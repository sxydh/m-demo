from typing import Any

import scrapy
from scrapy.http import Response


class TjbzApp(scrapy.Spider):
    name = "tjbz"
    start_urls = ["https://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2023/index.html"]

    def parse(self, response: Response, **kwargs: Any) -> Any:
        pass
