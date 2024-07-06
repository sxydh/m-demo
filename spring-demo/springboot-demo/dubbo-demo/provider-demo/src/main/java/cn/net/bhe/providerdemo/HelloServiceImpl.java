package cn.net.bhe.providerdemo;

import cn.net.bhe.mutil.NmUtils;
import cn.net.bhe.providerdemo.api.HelloService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "1.0.0")
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello() {
        return NmUtils.randomName();
    }

}
