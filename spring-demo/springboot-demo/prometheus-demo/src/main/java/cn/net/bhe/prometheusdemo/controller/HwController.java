package cn.net.bhe.prometheusdemo.controller;

import cn.net.bhe.mutil.StrUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hw")
public class HwController {

    private final MeterRegistry meterRegistry;
    private final Counter.Builder counterBuilder;

    public HwController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.counterBuilder = Counter.builder("http_hw");
    }

    @GetMapping
    public String get() {
        counterBuilder.description("HwController GET")
                .tag("method", "GET")
                .register(meterRegistry)
                .increment();
        return StrUtils.HELLO_WORLD;
    }

    @PostMapping
    public void post() {
        counterBuilder.description("HwController POST")
                .tag("method", "POST")
                .register(meterRegistry)
                .increment();
    }

}
