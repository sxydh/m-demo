import os


def mkdir_p(p):
    os.makedirs(p, exist_ok=True)


def touch(fp):
    with open(fp, 'a+', encoding='utf-8'):
        pass


def cat(fp):
    with open(fp, encoding='utf-8') as f:
        return f.read()


def vim(fp, t):
    with open(fp, 'a+', encoding='utf-8') as f:
        f.write(t)
