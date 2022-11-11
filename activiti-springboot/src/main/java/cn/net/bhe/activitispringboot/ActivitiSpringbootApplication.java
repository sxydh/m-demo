package cn.net.bhe.activitispringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

;

/**
 * @author Administrator
 */
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        org.activiti.spring.boot.SecurityAutoConfiguration.class})
public class ActivitiSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivitiSpringbootApplication.class, args);
    }

}
