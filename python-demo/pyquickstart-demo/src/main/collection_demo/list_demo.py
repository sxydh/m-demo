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
