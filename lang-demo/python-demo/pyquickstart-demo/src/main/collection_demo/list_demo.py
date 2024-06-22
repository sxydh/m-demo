from typing import Union

if __name__ == '__main__':
    # 创建列表
    list_ = ['a', 'b', 'c']
    print(list_)

    # 访问列表
    print(list_[0])
    print(list_[-1])

    # 遍历列表
    for item in list_:
        print(item)

    # 修改列表
    list_[1] = '-b'
    print(list_)
    list_.append('d')
    print(list_)
    list_.remove('a')
    print(list_)

    # 转换类型
    print(set(list_))
    print(tuple(list_))

    # 元素类型是可选的
    list2_: list[str] = ['a', 'b', 'c']
    print(list2_)
    list3_: list[Union[str, int]] = ['a', 'b', 'c', 1, 2, 3]
    print(list3_)
