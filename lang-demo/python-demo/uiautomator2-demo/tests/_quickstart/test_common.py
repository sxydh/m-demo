from unittest import TestCase

from _quickstart._quickstart_app import App, Session, Gesture, Selector


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


class TestGesture(TestCase):

    def setUp(self):
        self.gesture = Gesture()

    def test_click(self):
        self.gesture.click()

    def test_swipe(self):
        self.gesture.swipe()


class TestSelector(TestCase):

    def setUp(self):
        self.selector = Selector()

    def test_exits(self):
        self.selector.exits()

    def test_wait(self):
        self.selector.wait()

    def test_text(self):
        self.selector.text()

    def test_class_name(self):
        self.selector.class_name()

    def test_click(self):
        self.selector.click()
