package cn.net.bhe.jdkdemo.reflectdemo.invocationhandlerdemo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class HelloServiceHandler implements InvocationHandler {

    private final Object target;

    public HelloServiceHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(target, args);
    }

}
