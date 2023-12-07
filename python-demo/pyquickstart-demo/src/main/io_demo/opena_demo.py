import os

if __name__ == '__main__':
    file = open(os.path.join(os.path.dirname(__file__), 'tmp', 'open_demo.txt'), 'a', encoding='UTF-8')
    file.write('Hello World!')
    file.flush()
    file.close()
