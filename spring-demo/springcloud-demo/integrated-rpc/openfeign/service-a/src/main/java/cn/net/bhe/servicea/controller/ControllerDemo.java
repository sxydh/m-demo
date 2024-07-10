package cn.net.bhe.servicea.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/")
public class ControllerDemo {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public String get() {
        return "Service A " + restTemplate.getForObject("http://service-b/", String.class);
    }

}
