# Profile

H5人脸识别解决方案

## Quick Start

* 服务端  
  * 打开项目（建议IDEA），并加载相关依赖。
  * 配置百度人脸识别应用的[API Key](https://ai.baidu.com/ai-doc/REFERENCE/Ck3dwjgn3)和[Secret Key](https://ai.baidu.com/ai-doc/REFERENCE/Ck3dwjgn3)  
    application.yml

    ```yml
    frs:
      baidu:
        clientId: API Key
        clientSecret: Secret Key
    ```

  * 启动项目  
    H5FrsServerApplication.java

    ```java
    @SpringBootApplication
    public class H5FrsServerApplication {

        public static void main(String[] args) {
            SpringApplication.run(H5FrsServerApplication.class, args);
        }

    }
    ```

* WEB端
  * 初始化项目  

    ```bash
    yarn
    ```

  * 更改服务端地址  
    vue.config.js

    ```JavaScript
    devServer: {
      proxy: `http://ip:10086`
    }
    ```

  * 启动项目

    ```bash
    yarn run serve
    ```

* 人脸注册  
  手机访问`https://ip:20086/?bpage=register&buser=user01`  

* 人脸登录  
  手机访问`https://ip:20086/?bpage=login`  
