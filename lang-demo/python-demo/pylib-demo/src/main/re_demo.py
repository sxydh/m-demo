import re

from file_demo import cat
from file_demo import mkdir_p
from file_demo import vim


def is_w(c):
    return not c or re.match(r'[0-9a-zA-Z]|[`~!@#$%^&*()\-_=+\[{\]}\\|;:\'",<.>/?]', c)


def is_n(c):
    return c != '\r' and c != '\n' and c != '\r\n'


if __name__ == '__main__':
    p = 'tmp'
    mkdir_p(p)
    fpi = p + '/input.txt'
    fpo = p + '/output.txt'
    fs = cat(fpi)
    prev = None
    res = ''
    for fc in fs:
        b1 = not is_w(fc) and is_w(prev)
        b2 = is_w(fc) and not is_w(prev)
        b3 = b1 or b2
        b4 = prev and prev != ' ' and not is_n(prev)
        b5 = fc != ' ' and not is_n(fc)
        b = b3 and b4 and b5
        if b:
            res = res + ' ' + fc
        else:
            res = res + fc
        prev = fc
    vim(fpo, res)
