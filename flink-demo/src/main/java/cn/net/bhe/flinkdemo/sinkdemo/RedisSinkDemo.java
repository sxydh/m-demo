package cn.net.bhe.flinkdemo.sinkdemo;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

public class RedisSinkDemo {

    public static void main(String[] args) throws Exception {
        new RedisSinkDemo().wordSave();
    }

    public void wordSave() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据出口 */
        // 数据保存到Redis，Key和Value都是数据本身。
        dataStream.addSink(getRedisSink());

        /* 执行 */
        environment.execute();
    }

    private static RedisSink<String> getRedisSink() {
        FlinkJedisPoolConfig config = new FlinkJedisPoolConfig.Builder()
                .setHost("192.168.233.129")
                .setPort(6379)
                .build();
        return new RedisSink<>(config, new StringRedisMapper());
    }

    public static class StringRedisMapper implements RedisMapper<String> {

        @Override
        public RedisCommandDescription getCommandDescription() {
            return new RedisCommandDescription(RedisCommand.SET);
        }

        @Override
        public String getKeyFromData(String data) {
            return data;
        }

        @Override
        public String getValueFromData(String data) {
            return data;
        }
    }

}
