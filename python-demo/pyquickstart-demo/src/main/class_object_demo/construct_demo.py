class Man:
    name = None
    age = None

    # 类的构造方法
    def __init__(self, name, age):
        self.name = name
        self.age = age


if __name__ == '__main__':
    man = Man('Jack', 20)
    print(man.name)
    print(man.age)
