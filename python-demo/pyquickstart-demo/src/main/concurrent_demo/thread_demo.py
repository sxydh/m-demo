import threading
import time
from random import randint

if __name__ == '__main__':
    def f():
        while True:
            time.sleep(randint(1, 10))
            print(f'current_thread.id = {threading.current_thread().ident}, '
                  f'current_thread.name = {threading.current_thread().name} ')


    threading.Thread(target=f, name='thread-01', args=()).start()
    threading.Thread(target=f, name='thread-02', args=()).start()
    threading.Thread(target=f, name='thread-03', args=()).start()
