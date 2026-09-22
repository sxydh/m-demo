"""日志抽象：目前仅实现为直接打印"""


def log(*args, **kwargs):
    print(*args, **kwargs)
