package cn.net.bhe.aopdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public class ComponentDemo {

    @Autowired
    @Lazy
    private ComponentDemo componentDemo;

    public void run() {
        System.out.println("run");
    }

    @EventListener({ApplicationReadyEvent.class})
    public void onApplicationReady() {
        componentDemo.run();
    }

}
