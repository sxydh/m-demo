from src.main.util.cli import Cli
from src.main.util.common import get_sqlite_connection


class QcwyApp:

    def __init__(self):
        self.cli = Cli()
        self.conn = get_sqlite_connection()

    def close(self):
        self.cli.quit()
        self.conn.close()


if __name__ == '__main__':
    pass
