package cn.net.bhe.servicea.controller;

import cn.net.bhe.servicea.rpc.ServiceBRpc;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ControllerDemo {

    @Resource
    private ServiceBRpc serviceBRpc;

    @GetMapping
    public String get() {
        return "Service A " + serviceBRpc.get();
    }

}
