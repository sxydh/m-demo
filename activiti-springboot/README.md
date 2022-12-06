# Profile

SpringBoot2.7.2集成Activiti7.1.0.M6

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
  @SpringBootApplication
  public class ActivitiSpringbootApplication {

      public static void main(String[] args) {
          SpringApplication.run(ActivitiSpringbootApplication.class, args);
      }

  }
  ```

  > **Note**  
  >
  > `spring.activiti.database-schema-update`配置是否自动创建Activiti表  
  > `spring.activiti.db-history-used`配置是否创建act_hi_*表  
  > `spring.activiti.history-level`配置是否记录历史数据  
  >
  > resources/processes下的流程文件默认自动部署
