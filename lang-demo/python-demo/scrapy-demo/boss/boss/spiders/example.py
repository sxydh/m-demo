import copy
import logging
import os
import re
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
        if self.is_need_request_again(response):
            yield scrapy.Request(url=self.start_urls[0], callback=self.parse, dont_filter=True)
            return

        for city in self.cities:
            for industry in self.industries:
                for experience in self.experiences:
                    for degree in self.degrees:
                        for scale in self.scales:
                            url = f"{self.start_urls[0]}?city={city[0]}&industry={industry[0]}&experience={experience[0]}&degree={degree[0]}&scale={scale[0]}&page=1"
                            yield scrapy.Request(
                                url=url,
                                callback=self.parse_job_list,
                                meta={
                                    "meta_keep": {
                                        "city": city,
                                        "industry": industry,
                                        "experience": experience,
                                        "degree": degree,
                                        "scale": scale,
                                        "url": url}})
                            sleep(1)

    def parse_job_list(self, response: Response) -> Any:
        meta_keep = response.meta["meta_keep"]

        if self.is_need_request_again(response):
            yield scrapy.Request(url=meta_keep["url"], callback=self.parse_job_list, meta={"meta_keep": copy.copy(meta_keep)}, dont_filter=True)
            return

        cur_page = re.search(r"page=(\d+)", response.url)
        max_page = 0
        if cur_page:
            cur_page = int(cur_page.group(1))
        else:
            cur_page = 1
        pages = response.css(".options-pages a")
        if len(pages) > 2:
            max_page = int(pages[-2].css("::text").get().strip())

        job_list = response.css(".job-list-box .job-card-wrapper")
        for job in job_list:
            job_item = JobItem()
            job_item["name"] = self.parse_text_helper(job, ".job-name")
            job_item["address"] = self.parse_text_helper(job, ".job-area")
            job_item["salary"] = self.parse_text_helper(job, ".salary")
            job_item["company"] = self.parse_text_helper(job, ".company-name")
            job_item["city"] = meta_keep["city"][1]
            job_item["industry"] = meta_keep["industry"][1]
            job_item["experience"] = meta_keep["experience"][1]
            job_item["degree"] = meta_keep["degree"][1]
            job_item["scale"] = meta_keep["scale"][1]
            job_item["job_tag"] = self.parse_text_helper(job, ".job-card-footer", is_multi=True)
            job_item["company_tag"] = self.parse_text_helper(job, ".company-tag-list *", is_multi=True)
            yield job_item

        sleep(1)
        if cur_page and max_page and cur_page < max_page:
            meta_keep["url"] = f"{response.url.replace(f"page={cur_page}", f"page={cur_page + 1}")}"
            yield scrapy.Request(
                url=meta_keep["url"],
                callback=self.parse_job_list,
                meta={"meta_keep": copy.copy(meta_keep)})

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

    def is_need_request_again(self, response: Response) -> bool:
        sliders = response.css(".page-verify-slider")
        if len(sliders) > 0:
            logging.warning(f"### page-verify-slider ### {response.url}")
            return True
        return False


if __name__ == "__main__":
    sys.path.append(os.path.dirname(os.path.abspath(__file__)))
    execute(["scrapy", "crawl", "example", "-L", "WARN"])
