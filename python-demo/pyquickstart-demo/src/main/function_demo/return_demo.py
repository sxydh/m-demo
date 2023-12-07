# 返回None
# None是特殊的字面量
def f():
    return


# 返回多个值
def f2():
    return 1, '2', [3, 4]


if __name__ == '__main__':
    ret = f()
    print(f'{type(ret)} = {ret}')
    a, b, c = f2()
    print(f'{type(a)} = {a}')
    print(f'{type(b)} = {b}')
    print(f'{type(c)} = {c}')
