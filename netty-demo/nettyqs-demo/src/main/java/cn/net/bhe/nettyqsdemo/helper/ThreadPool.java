package cn.net.bhe.nettyqsdemo.helper;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {

    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(
            8,
            20,
            0,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(256),
            new ThreadFactory() {
                private final AtomicInteger tn = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("tp-%s", tn.incrementAndGet()));
                }
            },
            new ThreadPoolExecutor.AbortPolicy());

    public static Future<?> submit(Runnable runnable) {
        return THREAD_POOL.submit(runnable);
    }

}

