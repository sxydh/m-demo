from src.main.util.cli import Cli
from src.main.util.common import get_sqlite_connection


class QcwyApp:

    start_url = "https://we.51job.com/pc/search?searchType=2&sortType=1"
    job_areas = ["090200"]
    functions = []
    work_years = []
    degrees = []
    company_sizes = []

    def __init__(self):
        self.cli = Cli()
        self.conn = get_sqlite_connection()

    def start(self):
        pass

    def close(self):
        self.cli.quit()
        self.conn.close()


if __name__ == '__main__':
    QcwyApp().start()
