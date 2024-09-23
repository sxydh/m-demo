import os
import sys
from time import sleep
from typing import Any

import scrapy
from scrapy.cmdline import execute
from scrapy.http import Response

from boss.items import JobItem


class ExampleSpider(scrapy.Spider):
    name = "example"
    allowed_domains = []
    start_urls = ["https://www.zhipin.com/web/geek/job"]

    cities = [["101270100", "成都"]]
    industries = []
    experiences = [["103", "1年以内"], ["104", "1-3年"], ["105", "3-5年"], ["106", "5-10年"], ["107", "10年以上"]]
    degrees = [["209", "初中及以下"], ["208", "中专/中技"], ["206", "高中"], ["202", "大专"], ["203", "本科"], ["204", "硕士"], ["205", "博士"]]
    scales = [["301", "0-20人"], ["302", "20-99人"], ["303", "100-499人"], ["304", "500-999人"], ["305", "1000-9999人"], ["306", "10000人以上"]]

    def __init__(self, **kwargs: Any):
        super().__init__(**kwargs)
        # 数据来自 https://www.zhipin.com/wapi/zpCommon/data/industryFilterExemption
        with open("tmp/industry.csv", "r", encoding="utf-8") as f:
            for line in f.readlines():
                self.industries.append(line.strip().split(","))

    def parse(self, response: Response, **kwargs: Any) -> Any:
        for city in self.cities:
            for industry in self.industries:
                for experience in self.experiences:
                    for degree in self.degrees:
                        for scale in self.scales:
                            yield scrapy.Request(
                                url=f"{self.start_urls[0]}?city={city[0]}&industry={industry[0]}&experience={experience[0]}&degree={degree[0]}&scale={scale[0]}",
                                callback=self.parse_job_list,
                                meta={"meta_filter": {"city": city, "industry": industry, "experience": experience, "degree": degree, "scale": scale}})
                            sleep(1)

    def parse_job_list(self, response: Response) -> Any:
        meta_filter = response.meta["meta_filter"]

        job_list = response.css(".job-list-box .job-card-wrapper")
        for job in job_list:
            job_item = JobItem()
            job_item["name"] = self.parse_text_helper(job, ".job-name")
            job_item["address"] = self.parse_text_helper(job, ".job-area")
            job_item["salary"] = self.parse_text_helper(job, ".salary")
            job_item["company"] = self.parse_text_helper(job, ".company-name")
            job_item["city"] = meta_filter["city"][1]
            job_item["industry"] = meta_filter["industry"][1]
            job_item["experience"] = meta_filter["experience"][1]
            job_item["degree"] = meta_filter["degree"][1]
            job_item["scale"] = meta_filter["scale"][1]
            job_item["job_tag"] = self.parse_text_helper(job, ".job-card-footer", is_multi=True)
            job_item["company_tag"] = self.parse_text_helper(job, ".company-tag-list", is_multi=True)
            yield job_item

    def parse_text_helper(self, src, selector, is_multi=False, replaces: list = " ") -> str | None:
        text = None
        arr = src.css(selector)
        if len(arr) == 0:
            return text
        if not is_multi:
            text = arr[0].css("::text").get()
        else:
            arr = [ele.css("::text").get() for ele in arr]
            arr = [ele for ele in arr if ele]
            text = "###".join(arr)
        if replaces:
            for replace in replaces:
                text = text.replace(replace, "")
        return text


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "DEBUG"])
