package cn.net.bhe.rediscspringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redisc")
public class RediscController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/get")
    public String get(@RequestParam("key") String key) {
        return redisTemplate.opsForValue().get(key);
    }

}
