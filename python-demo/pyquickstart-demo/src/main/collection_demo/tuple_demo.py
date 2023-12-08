from typing import Union

if __name__ == '__main__':
    # 创建元组
    # 元组是不可变的
    # 元组中的逗号是必需的，即使只有一个元素。
    tuple_ = ('a', 'b', 'c')
    print(tuple_)

    # 访问元组
    print(tuple_[0])

    # 转换类型
    print(list(tuple_))
    print(set(tuple_))

    # 元素类型是可选的
    tuple2_: tuple[int, int, int] = (1, 2, 3)
    print(tuple2_)
    tuple3_: tuple[int, Union[int, str]] = (1, 'a')
    print(tuple3_)
