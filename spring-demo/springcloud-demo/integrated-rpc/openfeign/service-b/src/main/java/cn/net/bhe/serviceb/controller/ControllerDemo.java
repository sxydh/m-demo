package cn.net.bhe.serviceb.controller;

import cn.net.bhe.serviceb.rpc.ServiceARpc;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ControllerDemo {

    @Resource
    private ServiceARpc serviceARpc;

    @GetMapping
    public String get(@RequestParam(value = "from", required = false) String from) {
        String ret = "Service B";
        if (!"service-a".equals(from)) {
            ret += " " + serviceARpc.get("service-b");
        }
        return ret;
    }

}
