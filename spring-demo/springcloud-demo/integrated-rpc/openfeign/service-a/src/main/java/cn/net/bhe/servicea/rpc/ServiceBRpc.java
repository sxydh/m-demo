package cn.net.bhe.servicea.rpc;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-b")
public interface ServiceBRpc {

    @GetMapping
    String get(@RequestParam("from") String from);

}
