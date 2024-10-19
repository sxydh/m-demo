from unittest import TestCase

from Common import App, Session


class TestApp(TestCase):

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


class TestSession(TestCase):

    def setUp(self):
        self.session = Session()

    def test_session(self):
        self.session.session()
