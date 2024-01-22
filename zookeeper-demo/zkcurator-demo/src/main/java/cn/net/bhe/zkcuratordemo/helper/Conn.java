package cn.net.bhe.zkcuratordemo.helper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

public class Conn {

    private static final CuratorFramework CURATOR_FRAMEWORK;

    static {
        try {
            CURATOR_FRAMEWORK = CuratorFrameworkFactory.builder()
                    .connectString("hadoop01:2181,hadoop02:2181,hadoop03:2181")
                    .connectionTimeoutMs(20 * 1000)
                    .sessionTimeoutMs(60 * 1000)
                    .retryPolicy(new RetryNTimes(3, 1000))
                    .build();
            CURATOR_FRAMEWORK.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CuratorFramework get() {
        return CURATOR_FRAMEWORK;
    }

}
