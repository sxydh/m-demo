package cn.net.bhe.jdkdemo.selectordemo;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class _SelectorApp {

    public static void main(String[] args) throws Exception {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(8080));
            serverChannel.configureBlocking(false);
            try (Selector selector = Selector.open()) {
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                // noinspection InfiniteLoopStatement
                while (true) {
                    int i = selector.select(1000);
                    if (i == 0) {
                        continue;
                    }
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = keys.iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey next = keyIterator.next();
                        keyIterator.remove();
                        System.out.println(next);
                    }
                }
            }
        }
    }

}
