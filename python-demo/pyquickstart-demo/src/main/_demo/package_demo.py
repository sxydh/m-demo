if __name__ == '__main__':
    # 包是对多个模块的集中管理
    # 包物理上就是一个文件夹，该文件夹下包含了一个__init__.py文件（该文件用来表示当前文件夹是个包），和其它多个模块。
    # __init__.py文件通过__all__变量（该变量是个列表）来限定该文件夹下哪些模块对外可见

    # 导入整个包
    from json import *

    # 导入包中的一个模块
    import json.encoder as encode_
    from json import encoder as encode2_
    from json.encoder import *

    # 导入包中一个模块下的一个方法
    from json.encoder import py_encode_basestring_ascii as ascii

    print(dumps({'a': 1, 'b': 2}))
    print(encode_.py_encode_basestring_ascii('中文'))
    print(encode2_.py_encode_basestring_ascii('中文'))
    print(py_encode_basestring_ascii('中文'))
    print(ascii('中文'))
