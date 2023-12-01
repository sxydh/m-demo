package cn.net.bhe.flinkdemo.statedemo;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.executiongraph.restart.RestartStrategy;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class MemoryStateBackendDemo {

    public static void main(String[] args) throws Exception {
        new MemoryStateBackendDemo().intParse();
    }

    public void intParse() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 状态后端配置 */
        // 存储位置
        environment.setStateBackend(new HashMapStateBackend());
        /* 检查点配置 */
        // 间隔时间
        environment.enableCheckpointing(1000);
        // 高级选项
        environment.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
        environment.getCheckpointConfig().setCheckpointTimeout(10000L);
        environment.getCheckpointConfig().setMaxConcurrentCheckpoints(3);
        environment.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
        environment.getCheckpointConfig().setTolerableCheckpointFailureNumber(0);
        // 恢复策略
        environment.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 3000L));

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        DataStream<Integer> result = dataStream.map(Integer::parseInt);

        /* 数据出口 */
        result.print();

        /* 执行 */
        environment.execute();
    }

}
