package cn.net.bhe.consumer;

import cn.net.bhe.api.HelloService;
import cn.net.bhe.mutil.StrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.ConsumerBuilder;
import org.apache.dubbo.config.bootstrap.builders.ReferenceBuilder;
import org.apache.dubbo.config.bootstrap.builders.RegistryBuilder;

@Slf4j
public class _ConsumerApp {

    public static void main(String[] args) throws InterruptedException {
        ReferenceConfig<HelloService> reference = ReferenceBuilder.<HelloService>newBuilder()
                .interfaceClass(HelloService.class)
                .build();

        DubboBootstrap.getInstance()
                .application(_ConsumerApp.class.getName())
                .registry(RegistryBuilder.newBuilder()
                        .address("nacos://localhost:8848?qos.port=22223")
                        .build())
                .consumer(new ConsumerBuilder()
                        // 针对所有 reference 的过滤器
                        // 使用内置过滤器关键字 default 排序
                        // 此时自定义过滤器排在最后
                        .filter("default,anyFlag")
                        .build())
                .reference(reference)
                .start();

        HelloService service = reference.get();
        String message = StrUtils.ZERO;
        while (StrUtils.isNotEmpty(message)) {
            try {
                message = service.hello();
                log.info(message);
            } catch (Throwable t) {
                log.error(t.getLocalizedMessage(), t);
            } finally {
                // noinspection BusyWait
                Thread.sleep(3000);
            }
        }
    }

}
