package cn.net.bhe.provider;

import cn.net.bhe.api.HelloService;
import cn.net.bhe.mutil.NmUtils;

public class HelloServiceImpl implements HelloService {

    @Override
    public String hello() {
        return NmUtils.randomName();
    }

}
