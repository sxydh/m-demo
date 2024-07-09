package cn.net.bhe.springcloudgateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class _SpringCloudGatewayApp {

    public static void main(String[] args) {
        SpringApplication.run(_SpringCloudGatewayApp.class);
    }

}
