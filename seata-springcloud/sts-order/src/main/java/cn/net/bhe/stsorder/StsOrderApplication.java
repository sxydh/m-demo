package cn.net.bhe.stsorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("cn.net.bhe.stsfeign")
@MapperScan("cn.net.bhe.stsorder.mapper")
public class StsOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(StsOrderApplication.class, args);
    }

}
