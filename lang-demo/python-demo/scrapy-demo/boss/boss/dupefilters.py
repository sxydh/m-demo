from typing import Optional

from scrapy import Request
from scrapy.dupefilters import RFPDupeFilter
from scrapy.utils.request import RequestFingerprinterProtocol

from boss.util.common import get_sqlite_connection


class SqliteDupeFilter(RFPDupeFilter):

    def __init__(self,
                 path: Optional[str] = None,
                 debug: bool = False,
                 *,
                 fingerprinter: Optional[RequestFingerprinterProtocol] = None,
                 ):
        super().__init__(path, debug, fingerprinter=fingerprinter)
        self.conn = get_sqlite_connection()

    def request_seen(self, request: Request) -> bool:
        if super().request_seen(request):
            return True
        if self.conn.execute("select 1 from boss_job where job_list_url = ?", [request.url]).fetchone():
            return True

    def close(self, reason: str) -> None:
        super().close(reason)
        self.conn.close()
