def append(r):
    with open('tmp/output.txt', mode='a', encoding='utf-8') as o:
        o.write(r + '\r')


def append_e(r):
    with open('tmp/error.txt', mode='a', encoding='utf-8') as o:
        o.write(r + '\r')


def read_rows(f):
    with open(f'input/{f}', mode='r', encoding='utf-8') as i:
        return i.read().split('\r')
