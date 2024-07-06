package cn.net.bhe.providerdemo;

import cn.net.bhe.mutil.NmUtils;
import dubbodemo.api.HelloService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello() {
        return NmUtils.randomName();
    }

}
