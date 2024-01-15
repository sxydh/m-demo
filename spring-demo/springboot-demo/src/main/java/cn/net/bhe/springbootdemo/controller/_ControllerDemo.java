package cn.net.bhe.springbootdemo.controller;

import cn.net.bhe.mutil.StrUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class _ControllerDemo {

    @GetMapping("/get")
    public String get() {
        return StrUtils.HELLO_WORLD;
    }

}
