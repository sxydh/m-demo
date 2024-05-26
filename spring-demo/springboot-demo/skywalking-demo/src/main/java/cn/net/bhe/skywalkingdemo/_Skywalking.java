package cn.net.bhe.skywalkingdemo;

import cn.net.bhe.mutil.DbUtils;
import cn.net.bhe.mutil.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@SpringBootApplication
@Slf4j
@RestController
public class _Skywalking {

    public static void main(String[] args) {
        SpringApplication.run(_Skywalking.class);
    }

    private final DbUtils dbUtils = DbUtils.build("jdbc:mysql://192.168.233.129:3306/sw_demo", "root", "123");

    public _Skywalking() throws SQLException {
    }

    @GetMapping("/")
    public void api() throws Exception {
        List<HashMap<String, Object>> list = dbUtils.executeQuery("select uuid() as uuid");
        System.out.println(TraceContext.traceId() + " : " + list);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        Thread thread = new Thread(() -> {
            try {
                Random random = new Random();
                while (true) {
                    HttpUtils.get("http://localhost:20010/");
                    // noinspection BusyWait
                    Thread.sleep(random.nextInt(500));
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        });
        thread.start();
    }

}
