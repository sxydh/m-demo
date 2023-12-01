package cn.net.bhe.flinkdemo.sourcedemo;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SocketSourceDemo {

    public static void main(String[] args) throws Exception {
        new SocketSourceDemo().wordCount();
    }

    public void wordCount() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        DataStream<Tuple2<String, Integer>> result = dataStream.flatMap(new WordCountFunction())
                .keyBy(tuple2 -> tuple2.f0)
                .sum(1);

        /* 数据出口 */
        result.print();

        /* 执行 */
        environment.execute();
    }

}
