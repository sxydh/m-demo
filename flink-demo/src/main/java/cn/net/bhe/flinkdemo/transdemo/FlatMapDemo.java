package cn.net.bhe.flinkdemo.transdemo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class FlatMapDemo {

    public static void main(String[] args) throws Exception {
        new FlatMapDemo().wordToChar();
    }

    public void wordToChar() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        DataStream<Character> result = dataStream.flatMap(new WordToCharFunction());

        /* 数据出口 */
        result.print();

        /* 执行 */
        environment.execute();
    }

    public static class WordToCharFunction implements FlatMapFunction<String, Character> {

        @Override
        public void flatMap(String value, Collector<Character> out) {
            for (char c : value.toCharArray()) {
                out.collect(c);
            }
        }
    }

}
