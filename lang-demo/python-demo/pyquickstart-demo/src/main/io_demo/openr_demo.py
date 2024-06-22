import os

if __name__ == '__main__':
    file = open(os.path.join(os.path.dirname(__file__), 'tmp', 'open_demo.txt'), 'r', encoding='UTF-8')
    for line in file:
        print(line, end='')
    file.close()
