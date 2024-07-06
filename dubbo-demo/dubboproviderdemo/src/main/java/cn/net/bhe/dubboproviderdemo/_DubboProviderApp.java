package cn.net.bhe.dubboproviderdemo;

import cn.net.bhe.dubbointerfacedemo.HelloService;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.ProtocolBuilder;
import org.apache.dubbo.config.bootstrap.builders.ServiceBuilder;

public class _DubboProviderApp {

    public static void main(String[] args) {
        DubboBootstrap.getInstance()
                .application(_DubboProviderApp.class.getName())
                .protocol(ProtocolBuilder.newBuilder().name("tri").port(50052).build())
                .service(ServiceBuilder.newBuilder().interfaceClass(HelloService.class).ref(new HelloServiceImpl()).build())
                .start()
                .await();
    }

}
