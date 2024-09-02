package cn.net.bhe.mybatisdemo.service;

import cn.net.bhe.mybatisdemo.mapper.HelloWorldMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class HelloWorldServiceImpl implements HelloWorldService {

    @Resource
    private HelloWorldMapper helloWorldMapper;

    @PostConstruct
    public void init() {
        String string = helloWorldMapper.selectHelloWorld();
        System.out.println(string);

        Long nextval = helloWorldMapper.selectSeqHelloWorldNextval();
        System.out.println(nextval);
    }

}
