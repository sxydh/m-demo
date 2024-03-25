package cn.net.bhe.quickstartdemo.controller;

import cn.net.bhe.mutil.StrUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class _ControllerDemo {

    @GetMapping("/get")
    public String get() {
        return StrUtils.HELLO_WORLD;
    }

    @PostMapping("/post")
    public String post(@RequestBody Map<String, Object> map) {
        return map.toString();
    }

}
