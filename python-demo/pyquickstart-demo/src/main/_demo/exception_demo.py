if __name__ == '__main__':
    try:
        raise Exception('出现异常了')
    except Exception as e:
        print(e)
    finally:
        print('finally')