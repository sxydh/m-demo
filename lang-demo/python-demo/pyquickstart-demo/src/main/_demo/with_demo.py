import os

if __name__ == '__main__':
    # with关键字与Java的try -with-resources类似
    with open(os.path.join(os.path.abspath(__file__)), 'r', encoding='UTF-8') as file:
        for line in file:
            print(line)
