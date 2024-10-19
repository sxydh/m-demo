from unittest import TestCase

from Common import App


class TestCommon(TestCase):

    def setUp(self):
        self.app = App()

    def test_app_start(self):
        self.app.app_start()

    def test_app_wait(self):
        self.app.app_wait()

    def test_app_stop(self):
        self.app.app_start()
        self.app.app_stop()

    def test_current(self):
        self.app.app_current()

    def test_session(self):
        self.app.session()