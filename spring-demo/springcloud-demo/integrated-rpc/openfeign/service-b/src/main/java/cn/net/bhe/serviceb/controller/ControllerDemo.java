package cn.net.bhe.serviceb.controller;

import cn.net.bhe.serviceb.rpc.ServiceARpc;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ControllerDemo {

    @Resource
    private ServiceARpc serviceARpc;

    @GetMapping
    public String get() {
        return "Service B " + serviceARpc.get();
    }

}
