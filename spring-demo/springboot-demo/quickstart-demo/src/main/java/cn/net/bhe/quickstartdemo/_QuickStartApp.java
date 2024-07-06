package cn.net.bhe.quickstartdemo;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class _QuickStartApp {

    public static void main(String[] args) {
        SpringApplication.run(_QuickStartApp.class);
        log.info(_QuickStartApp.class);
    }

}
