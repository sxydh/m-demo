import scrapy


class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = ["anjuke.com"]
    start_urls = ["https://anjuke.com"]

    def parse(self, response):
        pass
