package cn.net.bhe.flinkdemo.transdemo;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class MapDemo {

    public static void main(String[] args) throws Exception {
        new MapDemo().wordLength();
    }

    public void wordLength() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        // map
        DataStream<Integer> result = dataStream.map(String::length);

        /* 数据出口 */
        result.print();

        /* 执行 */
        environment.execute();
    }

}
