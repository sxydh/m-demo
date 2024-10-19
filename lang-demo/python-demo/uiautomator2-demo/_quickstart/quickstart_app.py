import uiautomator2 as u2


class App:

    def __init__(self):
        self.d = u2.connect()

    def app_start(self):
        """
        启动应用

        :return:
        """

        self.d.app_start("com.android.settings")

    def app_wait(self):
        """
        等待应用运行

        :return:
        """

        pkg = "com.android.settings"
        self.d.app_start(pkg)
        pid = self.d.app_wait(pkg, timeout=10.0)
        if pid:
            print(pid)

    def app_stop(self):
        """
        停止应用

        :return:
        """

        self.d.app_stop("com.android.settings")

    def app_current(self):
        """
        获取当前包名

        :return:
        """

        print(self.d.app_current())


class Session:

    def __init__(self):
        self.d = u2.connect()

    def session(self):
        """
        创建会话，会话可以用于启动应用，监测应用，停止应用等。

        :return:
        """

        try:
            with self.d.session("com.android.settings") as sess:
                print(sess.app_current())
        except Exception as e:
            # 应用奔溃时得到异常
            print(e)


class Gesture:

    def __init__(self):
        self.d = u2.connect()

    def click(self):
        self.d.click(100, 100)

    def swipe(self):
        self.d.swipe(300, 100, 100, 100, steps=1)


class Selector:

    def __init__(self):
        self.d = u2.connect()

    def exits(self):
        self.d.app_start("com.android.settings")
        b = self.d(text="无障碍").exists(timeout=2.0)
        print(b)

    def wait(self):
        self.d.app_start("com.android.settings")
        b = self.d(text="无障碍").wait(timeout=2.0)
        print(b)

    def text(self):
        self.d.app_start("com.android.settings")
        target = self.d(text="无障碍")
        if target.exists(timeout=3.0):
            print(target.info)

    def class_name(self):
        self.d.app_start("com.android.settings")
        target = self.d(className="android.widget.TextView")
        if target.exists(timeout=3.0):
            print(target.info)

    def click(self):
        self.d.app_start("com.android.settings")
        target = self.d(text="无障碍")
        if target.exists(timeout=3.0):
            target.click()
