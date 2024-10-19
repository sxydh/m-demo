import uiautomator2 as u2


class Common:

    def __init__(self):
        self.d = u2.connect()

    def app_current(self):
        """
        获取当前包名

        :return:
        """

        print(self.d.app_current())
