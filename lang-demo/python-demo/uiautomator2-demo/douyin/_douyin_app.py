import time

import uiautomator2 as u2


class DouYinApp:

    def __init__(self):
        self.pkg = "com.ss.android.ugc.aweme"
        self.d = u2.connect()

    def start(self):
        self.d.app_start(self.pkg)


if __name__ == '__main__':
    DouYinApp()

    time.sleep(9999999)
