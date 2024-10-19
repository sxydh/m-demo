from unittest import TestCase

from common import Common


class TestCommon(TestCase):

    def setUp(self):
        self.common = Common()

    def test_app_start(self):
        self.common.app_start()

    def test_current(self):
        self.common.app_current()
