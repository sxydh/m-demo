from src.main.util.cli import Cli
from src.main.util.common import get_sqlite_connection, read_rows


class QcwyApp:
    start_url = "https://we.51job.com/pc/search?searchType=2&sortType=1"
    job_areas = ["090200"]
    functions = []
    work_years = [("02", "1-3年"), ("03", "3-5年"), ("04", "5-10年"), ("05", "10年以上"), ("06", "无需经验")]
    degrees = [("01", "初中及以下"), ("02", "高中/中技/中专"), ("03", "大专"), ("04", "本科"), ("05", "硕士"), ("06", "博士"), ("07", "无")]
    company_sizes = [("01", "少于50人"), ("02", "50-150人"), ("03", "150-500人"), ("04", "500-1000人"), ("05", "1000-5000人"), ("06", "5000-10000人"), ("07", "10000人以上")]

    def __init__(self):
        self.cli = Cli()
        self.conn = get_sqlite_connection()
        functions = read_rows('function.csv')

    def start(self):
        pass

    def close(self):
        self.cli.quit()
        self.conn.close()


if __name__ == '__main__':
    QcwyApp().start()
