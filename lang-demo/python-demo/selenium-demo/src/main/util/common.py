def append(r):
    with open('tmp/output.txt', mode='a', encoding='utf-8') as o:
        o.write(r + '\r')


def append_e(r):
    with open('tmp/error.txt', mode='a', encoding='utf-8') as o:
        o.write(r + '\r')
