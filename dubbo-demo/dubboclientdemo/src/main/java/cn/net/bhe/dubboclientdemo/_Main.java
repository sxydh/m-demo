package cn.net.bhe.dubboclientdemo;

import cn.net.bhe.dubbointerfacedemo.HelloService;
import cn.net.bhe.mutil.StrUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.bootstrap.builders.ReferenceBuilder;

@Log4j2
public class _Main {

    public static void main(String[] args) {
        ReferenceConfig<HelloService> reference = ReferenceBuilder.<HelloService>newBuilder()
                .interfaceClass(HelloService.class)
                .url("tri://localhost:50052")
                .build();

        DubboBootstrap.getInstance()
                .reference(reference)
                .start();

        HelloService service = reference.get();
        String message = StrUtils.ZERO;
        while (StrUtils.isNotEmpty(message)) {
            try {
                message = service.hello();
                log.info(message);
                Thread.sleep(1000);
            } catch (Throwable t) {
                log.error(t.getLocalizedMessage(), t);
            }
        }
    }

}
