package cn.net.bhe.dubboproviderdemo;

import cn.net.bhe.dubbointerfacedemo.HelloService;
import cn.net.bhe.mutil.NmUtils;

public class HelloServiceImpl implements HelloService {

    @Override
    public String hello() {
        return NmUtils.randomName();
    }

}
