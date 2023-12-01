package cn.net.bhe.flinkdemo.statedemo;

import cn.net.bhe.mutil.StrUtils;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class KeyedStateDemo {

    public static void main(String[] args) throws Exception {
        new KeyedStateDemo().wordLongest();
    }

    public void wordLongest() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        // 统计各分区当前最长的单词
        DataStream<String> result = dataStream
                .keyBy(value -> StrUtils.EMPTY)
                .flatMap(new WordLongestFunction());

        /* 数据出口 */
        result.print();

        /* 执行 */
        environment.execute();
    }

    public static class WordLongestFunction extends RichFlatMapFunction<String, String> {

        private ValueState<String> longestWord;

        @Override
        public void open(Configuration parameters) {
            longestWord = getRuntimeContext().getState(new ValueStateDescriptor<>("longestWord", String.class));
        }

        @Override
        public void flatMap(String value, Collector<String> out) throws Exception {
            if (StrUtils.compareTo(value, longestWord.value()) > 0) {
                out.collect(value);
                longestWord.update(value);
            }
        }

        @Override
        public void close() {
            longestWord.clear();
        }
    }

}
