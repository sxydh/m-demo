package cn.net.bhe.redisson.controller;

import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/* TODO */
@RestController
@RequestMapping("/redisson")
public class RedissonController {

    @Autowired
    private Redisson redisson;

    @GetMapping("/lock")
    public void lock(@RequestParam("key") String key, @RequestParam("leaseTime") int leaseTime) {
        redisson.getLock(key).lock(leaseTime, TimeUnit.SECONDS);
    }

}
