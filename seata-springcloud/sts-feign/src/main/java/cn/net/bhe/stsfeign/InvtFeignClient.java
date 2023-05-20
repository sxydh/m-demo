package cn.net.bhe.stsfeign;

import cn.net.bhe.stscommon.domain.Invt;
import cn.net.bhe.stscommon.domain.Ret;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sts-invt", fallback = InvtFeignClientFallBack.class)
public interface InvtFeignClient {

    @PutMapping("/invt/update")
    Ret<Invt> update(@RequestBody Invt invt);

}
