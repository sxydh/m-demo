import scrapy


class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = ["zhipin.com"]
    start_urls = ["https://zhipin.com"]

    def parse(self, response):
        pass
