package cn.net.bhe.redisdemo;

import cn.net.bhe.mutil.DtUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootApplication
public class _RedisApp {

    public static void main(String[] args) {
        SpringApplication.run(_RedisApp.class);
    }

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        redisTemplate.opsForValue().set("ts", DtUtils.ts17());
        System.out.println(redisTemplate.opsForValue().get("ts"));
    }

}
