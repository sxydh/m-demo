import os


def mkdir_p(p):
    os.makedirs(p, exist_ok=True)


def touch(fp):
    with open(fp, 'a'):
        pass


def cat(fp):
    with open(fp, 'r') as f:
        return f.read()


def vim(fp, t):
    with open(fp, 'a') as f:
        f.write(t)
