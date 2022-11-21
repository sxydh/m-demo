# Profile

SpringBoot2.7.2集成Activiti6.0.0

## Quick Start

* 配置数据库  
  application.properties  

  ```Properties
  spring.datasource.druid.url=jdbc:mysql://localhost:3306/actboot?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&  serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true
  spring.datasource.druid.username=root
  spring.datasource.druid.password=123456
  spring.datasource.druid.driver-class-name=com.mysql.cj.jdbc.Driver
  ```

* 启动项目  
  ActivitiSpringbootApplication.java

  ```java
  @SpringBootApplication(exclude = {
          SecurityAutoConfiguration.class,
          org.activiti.spring.boot.SecurityAutoConfiguration.class})
  public class ActivitiSpringbootApplication {
  
      public static void main(String[] args) {
          SpringApplication.run(ActivitiSpringbootApplication.class, args);
      }
  
  }
  ```

  > **Note**  
  >
  > 首次运行时Activiti表会自动初始化  
  > resources/processes下的流程文件默认自动部署
