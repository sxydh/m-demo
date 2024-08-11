# 返回None
# None是特殊的字面量
def f():
    return


# 返回单个值
# 返回类型是可选的
def f2() -> int:
    return 1


# 返回多个值
def f3():
    return 1, '2', [3, 4]


if __name__ == '__main__':
    ret = f()
    print(f'f(): {type(ret)} = {ret}')

    ret = f2()
    print(f'f2(): {type(ret)} = {ret}')

    a, b, c = f3()
    print(f'f3(): {type(a)} = {a}, {type(b)} = {b}, {type(c)} = {c}')
