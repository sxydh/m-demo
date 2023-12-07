class Class:
    def f(self):
        pass


class SubClass(Class):

    def f(self):
        print("SubClass.f()")


class SubClass2(Class):

    def f(self):
        print("SubClass2.f()")


def f(clazz: Class):
    clazz.f()


if __name__ == '__main__':
    f(SubClass())
    f(SubClass2())
