from typing import Union

if __name__ == '__main__':
    # 创建字典
    dict_ = {'one': 1, 'two': 2, 'three': 3}
    print(dict_)

    # 访问字典
    print(dict_['one'])

    # 修改字典
    dict_['four'] = 4
    print(dict_)

    # 转换类型
    print(list(dict_.values()))
    print(set(dict_.values()))
    print(tuple(dict_.values()))

    # 元素类型是可选的
    dict2_: dict[str, int] = {'one': 1, 'two': 2, 'three': 3}
    print(dict2_)
    dict3_: dict[str, Union[int, str]] = {'one': 1, 'two': 2, 'three': 3, 'four': '4'}
    print(dict3_)
