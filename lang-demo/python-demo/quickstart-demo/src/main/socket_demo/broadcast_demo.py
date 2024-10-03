import socket
import threading
import time
import uuid

from m_pyutil import mip


class Sender(threading.Thread):

    def __init__(self, port: int = 8080):
        self.port = port
        super().__init__()

    def run(self):
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
            sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
            broadcast = mip.get_broadcast()
            count = 0
            while count < 10000:
                sock.sendto(str(uuid.uuid4()).encode('utf-8'), (broadcast, self.port))
                count += 1
                time.sleep(1)


class Receiver(threading.Thread):

    def __init__(self, port: int = 8080):
        self.port = port
        super().__init__()

    def run(self):
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
            sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            sock.bind(('', 8080))
            while True:
                data, address = sock.recvfrom(4096)
                print(f'Received message: {data.decode('utf-8')} from {address}')


if __name__ == '__main__':
    Receiver().start()
    Sender().start()
