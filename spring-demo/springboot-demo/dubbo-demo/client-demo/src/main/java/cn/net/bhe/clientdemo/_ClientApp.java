package cn.net.bhe.clientdemo;

import dubbodemo.api.HelloService;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableDubbo
@Log4j2
public class _ClientApp {

    public static void main(String[] args) {
        SpringApplication.run(_ClientApp.class);
    }

    @DubboReference
    private HelloService helloService;

    @EventListener(ApplicationReadyEvent.class)
    private void onApplicationReady() {
        new Thread(() -> {
            while (true) {
                String ret = helloService.hello();
                log.info(ret);
                try {
                    //noinspection BusyWait
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}
