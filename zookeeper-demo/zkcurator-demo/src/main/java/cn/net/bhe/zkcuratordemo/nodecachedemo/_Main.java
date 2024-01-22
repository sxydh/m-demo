package cn.net.bhe.zkcuratordemo.nodecachedemo;

import cn.net.bhe.zkcuratordemo.helper.Conn;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;

import java.nio.charset.StandardCharsets;

public class _Main {

    public static void main(String[] args) throws Exception {
        CuratorFramework curatorFramework = Conn.get();
        NodeCache nodeCache = new NodeCache(curatorFramework, "/nodecachedemo");
        StringBuffer data = new StringBuffer();
        nodeCache.getListenable().addListener(() -> {
            data.insert(0, " <= ").insert(0, new String(nodeCache.getCurrentData().getData(), StandardCharsets.UTF_8));
            System.out.println(data);
        });
        nodeCache.start();
        Thread.currentThread().join();
    }

}
