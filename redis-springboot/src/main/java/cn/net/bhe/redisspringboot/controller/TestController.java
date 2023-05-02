package cn.net.bhe.redisspringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/get")
    public String get(@RequestParam String key) {
        return Objects.toString(redisTemplate.keys("*"));
    }

}
