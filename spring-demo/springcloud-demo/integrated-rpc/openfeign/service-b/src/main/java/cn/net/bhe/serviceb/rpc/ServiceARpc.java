package cn.net.bhe.serviceb.rpc;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-a")
public interface ServiceARpc {

    @GetMapping
    String get(@RequestParam String from);

}
