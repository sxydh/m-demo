def append(r, f='output.txt'):
    with open(f'tmp/{f}', mode='a', encoding='utf-8') as o:
        o.write(r + '\n')


def append_e(r, f='error.txt'):
    with open(f'tmp/{f}', mode='a', encoding='utf-8') as o:
        o.write(r + '\n')


def read_rows(f='input.txt'):
    with open(f'tmp/{f}', mode='r', encoding='utf-8') as i:
        return i.read().split('\n')
