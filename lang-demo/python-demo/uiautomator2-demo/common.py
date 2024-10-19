import uiautomator2 as u2


class Common:

    def __init__(self):
        self.d = u2.connect()

    def app_start(self):
        """
        启动应用

        :return:
        """

        self.d.app_start("com.android.settings")

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
