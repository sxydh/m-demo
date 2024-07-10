package cn.net.bhe.serviceb.rpc;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "service-a")
public interface ServiceARpc {

    @GetMapping
    String get();

}
