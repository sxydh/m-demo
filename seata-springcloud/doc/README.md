# 快速上手

这是一个微服务（Spring Cloud Alibaba）集成Seata的简单例子。  
Seata官方也有类似的[*samples*](https://github.com/seata/seata-samples/tree/master/spring-cloud-alibaba-samples)。  

按以下步骤启动：  

* [*数据库*](sts.sql)初始化
* 启动[*SeaTa*](https://github.com/seata/seata/releases)
* 启动[*订单*](../sts-order/src/main/java/cn/net/bhe/stsorder/StsOrderApplication.java)服务
* 启动[*库存*](../sts-invt/src/main/java/cn/net/bhe/stsinvt/StsInvtApplication.java)服务
* 调用下单接口  

  ```bash
  curl --location 'http://127.0.0.1:50010/order/add' \
  --header 'Content-Type: application/json' \
  --data '{
      "goodsId": "fb76ba63-795a-4231-ab8f-f9071dab6363",
      "quantity": 1
  }'
  ```
