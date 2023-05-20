package cn.net.bhe.stsfeign;

import cn.net.bhe.stscommon.domain.Invt;
import cn.net.bhe.stscommon.domain.Ret;
import org.springframework.stereotype.Service;

@Service
public class InvtFeignClientFallBack implements InvtFeignClient {

    @Override
    public Ret<Invt> update(Invt invt) {
        return Ret.fail("更新库存失败");
    }

}
