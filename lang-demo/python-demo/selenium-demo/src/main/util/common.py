import datetime


def add_days(ds, delta):
    d = datetime.datetime.strptime(ds, '%Y-%m-%d')
    return (d + datetime.timedelta(days=delta)).strftime('%Y-%m-%d')


def write(r, mode, f):
    with open(f'tmp/{f}', mode=mode, encoding='utf-8') as o:
        o.write(r)


def append(r, f='output.txt'):
    write(r=f'{r}\n', mode='a', f=f)


def append_e(r, f='error.txt'):
    append(r=r, f=f)


def read(f):
    with open(f'tmp/{f}', mode='r', encoding='utf-8') as i:
        return i.read()


def read_rows(f='input.txt'):
    return read(f).split('\n')
