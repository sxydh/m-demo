import random
import socket
import threading
import time


def server_init():
    print('server init')
    socket_server = socket.socket()
    socket_server.bind(('localhost', 10006))
    socket_server.listen(10)

    def accept_conn(s: socket):
        while True:
            # accept阻塞获取连接
            (conn, address) = s.accept()
            print(f'accept connection: {conn}')
            threading.Thread(target=recv_data, args=(conn, address)).start()

    def recv_data(conn: socket, address: tuple):
        while True:
            # 阻塞获取数据
            data = conn.recv(1024)
            print(f'from client {address}: {data.decode('utf-8')}')

    threading.Thread(target=accept_conn, args=(socket_server,)).start()


def client_init():
    print('client init')
    socket_client = socket.socket()
    socket_client.connect(('localhost', 10006))
    while True:
        time.sleep(random.randint(1, 10))
        msg = str(random.randint(1, 1000))
        socket_client.send(msg.encode('utf-8'))


if __name__ == '__main__':
    server_init()
    time.sleep(1)
    client_init()
