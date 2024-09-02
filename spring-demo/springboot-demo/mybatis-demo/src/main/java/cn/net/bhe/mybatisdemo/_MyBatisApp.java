package cn.net.bhe.mybatisdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.net.bhe.mybatisdemo.mapper")
public class _MyBatisApp {

    public static void main(String[] args) {
        SpringApplication.run(_MyBatisApp.class);
    }

}
