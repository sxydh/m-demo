package cn.net.bhe.provider;

import cn.net.bhe.api.HelloService;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.ProtocolBuilder;
import org.apache.dubbo.config.bootstrap.builders.ServiceBuilder;

public class _ProviderApp {

    public static void main(String[] args) {
        DubboBootstrap.getInstance()
                .application(_ProviderApp.class.getName())
                .protocol(ProtocolBuilder.newBuilder().name("tri").port(50052).build())
                .service(ServiceBuilder.newBuilder().interfaceClass(HelloService.class).ref(new HelloServiceImpl()).build())
                .start()
                .await();
    }

}
