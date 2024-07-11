package cn.net.bhe.provider;

import cn.net.bhe.api.HelloService;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.ProtocolBuilder;
import org.apache.dubbo.config.bootstrap.builders.RegistryBuilder;
import org.apache.dubbo.config.bootstrap.builders.ServiceBuilder;

public class _ProviderApp {

    public static void main(String[] args) {
        DubboBootstrap.getInstance()
                .application(_ProviderApp.class.getName())
                .registry(RegistryBuilder.newBuilder()
                        .address("nacos://localhost:8848")
                        .build())
                .protocol(ProtocolBuilder.newBuilder()
                        .name("dubbo")
                        .port(-1)
                        .build())
                .service(ServiceBuilder.newBuilder()
                        .interfaceClass(HelloService.class)
                        .ref(new HelloServiceImpl())
                        .build())
                .start()
                .await();
    }

}
