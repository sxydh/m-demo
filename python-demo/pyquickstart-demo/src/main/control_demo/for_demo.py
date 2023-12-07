if __name__ == '__main__':
    # 快速开始
    for i in range(10):
        print(i, end='')
    print()

    # 获取下标
    list_ = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    for (i, e) in enumerate(list_):
        if e % 2 == 0:
            continue
        print('list[%i] = %s' % (i, e))
