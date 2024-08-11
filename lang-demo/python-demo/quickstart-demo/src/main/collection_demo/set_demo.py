from typing import Union

if __name__ == '__main__':
    # 创建集合
    set_ = {1, 2, 2}
    print(set_)

    # 遍历集合
    for item in set_:
        print(item)

    # 修改集合
    set_.add(3)
    print(set_)

    # 转换类型
    print(list(set_))
    print(tuple(set_))

    # 元素类型是可选的
    set2_: set[int] = {1, 2, 2}
    print(set2_)
    set3_: set[Union[int, str]] = {1, 2, 2, '2'}
    print(set3_)
