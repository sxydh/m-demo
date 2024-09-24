import scrapy


class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = ["www.baidu.com"]
    start_urls = ["https://www.baidu.com"]

    def parse(self, response):
        pass
