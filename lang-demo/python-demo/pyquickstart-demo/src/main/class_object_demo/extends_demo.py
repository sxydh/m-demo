class Class:
    name = 'Class'

    def __init__(self):
        print('init Class')

    def f(self):
        print(f'Class.f(): name = {self.name}')


class Class2:
    name = 'Class2'

    def __init__(self):
        print('init Class2')

    def f(self):
        print(f'Class2.f(): name = {self.name}')


# 单继承
class Class3(Class):

    # 子类默认继承父类构造函数，子类实例化时，父类构造函数会执行。
    # 子类可以定义自己的构造函数。如果子类构造函数没有显示调用父类构造函数，子类实例化时，父类构造函数并不会执行。
    def __init__(self):
        print('init Class3')

    def f(self):
        print(f'Class3.f(): name = {self.name}')


# 多继承
# 如果出现成员冲突，按从左到右的优先顺序保留。
class Class4(Class2, Class):
    # pass是空语句，可以用来保持程序结构的完整性。
    pass

    def __init__(self):
        # 显示调用父类构造函数
        super().__init__()
        print('init Class4')

    def f(self):
        print(f'Class4.f(): name = {self.name}')


if __name__ == '__main__':
    Class3().f()
    Class4().f()

    # 输出：
    # init Class3
    # Class3.f(): name = Class
    # init Class2
    # init Class4
    # Class4.f(): name = Class2
