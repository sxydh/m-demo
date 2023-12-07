# 空参数
def f():
    print()


# 有参数
def f2(o):
    print(f'{type(o)} = {o}')


# 有参数且带默认值
def f3(a=1, b='a'):
    print(f'{type(a)} = {a}')
    print(f'{type(b)} = {b}')


# 可变参数
def f4(*o):
    print(f'{type(o)} = {o}')


# 关键字参数
def f5(**kwargs):
    print(f'{type(kwargs)} = {kwargs}')


# 函数作为参数
def f6(func, o):
    print(f'{type(func)} = {func}')
    func(o)


if __name__ == '__main__':
    f()
    f2(1)
    f3(b='-a', a=-1)
    f4(1, 2, 3)
    f5(a=1, b='a', c=[1, 'a'])
    f6(lambda o: print(o), 1)
