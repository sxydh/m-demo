import re

from file_demo import mkdir_p


def is_w(c):
    return re.match(r'[0-9a-zA-Z]|[`~!@#$%^&*()\-_=+\[{\]}\\|;:\'",<.>/?]', c)


if __name__ == '__main__':
    mkdir_p('tmp')
