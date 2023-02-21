# 基于Netty实现内网穿透

## 快速开始

* 打包服务端

  ```shell
  gradlew build
  ```

* 运行服务端

  ```shell
  java -jar proxy-netty-1.0-SNAPSHOT.jar
  ```

* 客户端测试  
  * 将[index.html](./ui-ngx/index.html)部署到Nginx
  * 使用[TCP工具](https://github.com/sinpolib/sokit)发送消息
  * 打开`http://ip:port/index.html`回显消息
