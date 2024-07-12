package cn.net.bhe.provider;

import cn.net.bhe.api.HelloService;
import cn.net.bhe.mutil.NmUtils;

import java.util.Random;

public class HelloServiceImpl implements HelloService {

    private final Random random = new Random();

    @Override
    public String hello() {
        if (random.nextBoolean()) {
            throw new RuntimeException();
        }
        return NmUtils.randomName();
    }

}
