package cn.net.bhe.jdkdemo.reflectdemo.invocationhandlerdemo;

import java.lang.reflect.Proxy;

public class _InvocationHandlerApp {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();

        HelloService helloServiceProxy = (HelloService) Proxy.newProxyInstance(
                HelloService.class.getClassLoader(),
                new Class[]{HelloService.class},
                new HelloServiceHandler(helloService));

        helloServiceProxy.sayHello();
    }

}
