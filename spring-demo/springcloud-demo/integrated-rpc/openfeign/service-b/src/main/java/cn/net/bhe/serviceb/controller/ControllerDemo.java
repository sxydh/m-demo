package cn.net.bhe.serviceb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ControllerDemo {

    @GetMapping
    public String get() {
        return "Service B";
    }

}
