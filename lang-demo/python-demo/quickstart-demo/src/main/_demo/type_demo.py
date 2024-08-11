if __name__ == '__main__':
    # 数字
    int_ = 1
    print(type(int_), int_)
    complex_ = 1 + 2j
    print('%s = %s' % (type(complex_), complex_))
    bool_ = True
    print('%s = %s' % (type(bool_), bool_))

    # 字符串
    str_ = '1'
    print('%s = %s' % (type(str_), str_))

    # 列表
    list_ = [1, 2, 2]
    print('%s = %s' % (type(list_), list_))

    # 元组
    tuple_ = (1, 2, 3)
    print('%s = %s' % (type(tuple_), tuple_))

    # Set
    set_ = {1, 2, 2}
    print('%s = %s' % (type(set_), set_))

    # 字典
    dict_ = {'1': 1, '2': 2}
    print('%s = %s' % (type(dict_), dict_))