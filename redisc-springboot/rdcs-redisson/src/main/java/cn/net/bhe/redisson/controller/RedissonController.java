package cn.net.bhe.redisson.controller;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/redisson")
public class RedissonController {

    @Autowired
    private RedissonClient redissonClient;

    @GetMapping("/lock")
    public void lock(@RequestParam("key") String key, @RequestParam("leaseTime") int leaseTime) {
        redissonClient.getLock(key).lock(leaseTime, TimeUnit.SECONDS);
    }

}
