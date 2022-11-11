# Profile

SpringBoot2.7.2集成Activiti6.0.0

## Quick Start

* 配置数据库  
  activiti.cfg.xml  

  ```xml
  <property name="databaseType" value="oracle"></property>
  <property name="jdbcUrl" value="jdbc:oracle:thin:@127.0.0.1:1521/orcl"></property>
  <property name="jdbcDriver" value="oracle.jdbc.driver.OracleDriver"></property>
  <property name="jdbcUsername" value="bhe"></property>
  <property name="jdbcPassword" value="123"></property>
  <property name="databaseSchema" value="BHE"></property>
  <property name="databaseSchemaUpdate" value="true"></property>
  ```

  application.properties  

  ```Properties
  spring.datasource.druid.url=jdbc:oracle:thin:@127.0.0.1:1521/orcl
  spring.datasource.druid.username=bhe
  spring.datasource.druid.password=123
  spring.datasource.druid.driver-class-name=oracle.jdbc.OracleDriver
  ```

* 初始化Activiti表  
  ProcessEngineBuildApplication.java

  ```java
  public class ProcessEngineBuildApplication {
  
      public static void main(String[] args) {
          ProcessEngineConfiguration configuration = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
          ProcessEngine processEngine = configuration.buildProcessEngine();
          System.out.println(processEngine);
      }
  
  }
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
