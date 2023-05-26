# 快速上手

SpringBoot使用Redis集群简单示例  

## Redis集群搭建

* 安装Redis  
  此示例Redis安装位置为`/root/app/redis/redis-6.2.4`

* 准备集群配置文件  
  此示例搭建3主3从架构，端口分别为6381，6382，6383，6384，6385，6386。  
  在`/root/app/redis/redis-cluster/`下新建6个配置文件`redis1.conf`，`redis2.conf`，`redis3.conf`，`redis4.conf`，`redis5.conf`，`redis6.conf`。  
  `redis1.conf`内容示例：  

  ```bash
  bind 0.0.0.0
  port 6381
  daemonize yes
  logfile "/root/app/redis/redis-cluster/redis1.log"
  dbfilename "redis1.rdb"
  dir "/root/app/redis/redis-6.2.4"
  appendonly yes
  appendfilename "appendonly1.aof"
  
  cluster-enabled yes
  cluster-config-file node1.conf
  cluster-node-timeout 20000
  cluster-announce-ip 192.168.30.130
  cluster-announce-port 6381
  cluster-announce-bus-port 16381
  ```

* 启动Redis实例  

  ```bash
  /root/app/redis/redis-6.2.4/src/redis-server redis1.conf
  /root/app/redis/redis-6.2.4/src/redis-server redis2.conf
  /root/app/redis/redis-6.2.4/src/redis-server redis3.conf
  /root/app/redis/redis-6.2.4/src/redis-server redis4.conf
  /root/app/redis/redis-6.2.4/src/redis-server redis5.conf
  /root/app/redis/redis-6.2.4/src/redis-server redis6.conf
  ```

* 创建集群  
  
  ```bash
  /root/app/redis/redis-6.2.4/src/redis-cli --cluster create 192.168.30.130:6381 192.168.30.130:6382 192.168.30.130:6383 192.168.30.130:6384 192.168.30.130:6385 192.168.30.130:6386 --cluster-replicas 1
  ```

  查看集群状态：

  ```bash
  /root/app/redis/redis-6.2.4/src/redis-cli --cluster check 192.168.30.130:6381
  ```

  进入单节点：  

  ```bash
  /root/app/redis/redis-6.2.4/src/redis-cli -c -p 6381
  ```

## Lettuce客户端

* 配置文件application.yaml  
  
  ```yaml
  spring:
    redis:
      client-type: lettuce
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
      cluster:
        max-redirects: 3
        nodes: 192.168.30.130:6381,192.168.30.130:6382,192.168.30.130:6383,192.168.30.130:6384,192.168.30.130:6385,192.168.30.130:6386
  ```

* 启动项目
* 使用`RedisTemplate`操作Redis

## Redisson客户端

* 配置文件application.yaml  

  ```yaml
  spring:
    redis:
      redisson:
        file: classpath:redisson.yaml
  ```

  配置文件redisson.yaml  

  ```yaml
  # https://github.com/redisson/redisson/wiki/2.-Configuration#242-cluster-yaml-config-format
  clusterServersConfig:
    idleConnectionTimeout: 10000
    connectTimeout: 10000
    timeout: 3000
    retryAttempts: 3
    retryInterval: 1500
    failedSlaveReconnectionInterval: 3000
    failedSlaveCheckInterval: 60000
    password: null
    subscriptionsPerConnection: 5
    clientName: null
    loadBalancer: !<org.redisson.connection.balancer.RoundRobinLoadBalancer> {}
    subscriptionConnectionMinimumIdleSize: 1
    subscriptionConnectionPoolSize: 50
    slaveConnectionMinimumIdleSize: 24
    slaveConnectionPoolSize: 64
    masterConnectionMinimumIdleSize: 24
    masterConnectionPoolSize: 64
    readMode: "SLAVE"
    subscriptionMode: "SLAVE"
    nodeAddresses:
      - "redis://192.168.30.130:6381"
      - "redis://192.168.30.130:6382"
      - "redis://192.168.30.130:6383"
      - "redis://192.168.30.130:6384"
      - "redis://192.168.30.130:6385"
      - "redis://192.168.30.130:6386"
    scanInterval: 1000
    pingConnectionInterval: 30000
    keepAlive: false
    tcpNoDelay: true
  threads: 16
  nettyThreads: 32
  codec: !<org.redisson.codec.Kryo5Codec> {}
  transportMode: "NIO"
  ```

* 启动项目
* 使用`RedissonClient`操作Redis
