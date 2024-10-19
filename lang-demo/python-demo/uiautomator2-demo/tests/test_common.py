from unittest import TestCase

from common import Common


class TestCommon(TestCase):

    def test_current(self):
        Common().current()
