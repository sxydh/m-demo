if __name__ == '__main__':
    # 模块就是Python文件
    # 可以导入模块文件中的类/变量/函数等
    # 模块文件通过__all__变量（该变量是个列表）来限定哪些内容对外可见

    # 导入整个模块
    import math

    # 导入模块中的一个方法
    from time import localtime as lt

    # 导入模块中的所有方法
    from _md5 import *

    print(math.sqrt(2))
    print(lt())
    print(md5('123456'.encode()).hexdigest())
