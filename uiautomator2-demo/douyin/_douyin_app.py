import time

import uiautomator2 as u2


class DouYinApp:

    def __init__(self):
        self.pkg = "com.ss.android.ugc.aweme"
        self.d = u2.connect()

    def start(self):
        self.d.app_start(self.pkg)
        self.d.app_wait(self.pkg, timeout=10.0)

        while 1:
            self.d.swipe(100, 300, 100, 100, steps=1)
            time.sleep(1)


if __name__ == "__main__":
    DouYinApp().start()
