package cn.net.bhe.logstashdemo;

import cn.net.bhe.mutil.CpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@Slf4j
public class _Main {

    public static void main(String[] args) {
        SpringApplication.run(_Main.class);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        threadPool.submit(() -> {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    log.info(CpUtils.ranChnCp());
                    //noinspection BusyWait
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                // TODO NOTHING
            }
        });
    }

}
