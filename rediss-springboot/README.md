# 快速上手

SpringBoot集成Redis主从高可用简单示例

## Redis主从搭建

此示例使用1主2从架构，Redis安装路径为`/root/app/redis/redis-6.2.4`。  
在`/root/app/redis/sentinel/`下准备3个redis配置文件`redis1.conf`，`redis2.conf`，`redis3.conf`。  

`redis1.conf`作为master实例配置：  

```bash
bind 0.0.0.0
port 6381
daemonize yes

logfile "/root/app/redis/sentinel/redis1.log"
dbfilename "redis1.rdb"
dir "/root/app/redis/redis-6.2.4"
appendonly yes
appendfilename "redis1.aof"
```

`redis2.conf`，`redis3.conf`作为slave实例配置，其中`redis2.conf`内容：  

```bash
bind 0.0.0.0
port 6382
daemonize yes

logfile "/root/app/redis/sentinel/redis2.log"
dbfilename "redis2.rdb"
dir "/root/app/redis/redis-6.2.4"
appendonly yes
appendfilename "redis2.aof"
replicaof 192.168.30.130 6381
```

`redis3.conf`与`redis2.conf`类似。  

启动Redis主从实例：  

```bash
/root/app/redis/redis-6.2.4/src/redis-server redis1.conf
/root/app/redis/redis-6.2.4/src/redis-server redis2.conf
/root/app/redis/redis-6.2.4/src/redis-server redis3.conf
```

## Sentinel哨兵集群搭建

在`/root/app/redis/sentinel/`下准备3个Sentinel配置`sentinel1.conf`，`sentinel2.conf`，`sentinel3.conf`。  
`sentinel1.conf`内容为：  

```bash
port 26381
bind 0.0.0.0
daemonize yes
pidfile "/root/app/redis/sentinel/sentinel1.pid"
logfile "/root/app/redis/sentinel/sentinel1.log"
dir "/tmp"
sentinel monitor mymaster 192.168.30.130 6381 2
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 30000
```

`sentinel2.conf`，`sentinel3.conf`与`sentinel1.conf`类似。

准备Sentinel日志文件：  

```bash
touch sentinel1.log
touch sentinel2.log
touch sentinel3.log
```

启动Sentinel哨兵集群：  

```bash
/root/app/redis/redis-6.2.4/src/redis-server sentinel1.conf --sentinel
/root/app/redis/redis-6.2.4/src/redis-server sentinel2.conf --sentinel
/root/app/redis/redis-6.2.4/src/redis-server sentinel3.conf --sentinel
```

## SpringBoot集成

修改配置文件`application.yaml`：  

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
    sentinel:
      master: mymaster
      nodes: 192.168.30.130:26381,192.168.30.130:26382,192.168.30.130:26383
```

启动SpringBoot，使用`RedisTemplate`操作Redis。
