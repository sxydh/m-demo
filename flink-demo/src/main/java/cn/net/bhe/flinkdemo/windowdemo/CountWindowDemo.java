package cn.net.bhe.flinkdemo.windowdemo;

import cn.net.bhe.mutil.StrUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class CountWindowDemo {

    public static void main(String[] args) throws Exception {
        new CountWindowDemo().wordConcat();
    }

    public void wordConcat() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        DataStream<String> result = dataStream
                .keyBy(value -> StrUtils.EMPTY)
                // 滑动计数窗口
                // 窗口大小5，滑动步长2。
                // 每隔2个单词拼接后续5个单词
                .countWindow(5, 2)
                .aggregate(new WordConcatFunction());

        /* 数据出口 */
        result.print();

        /* 执行 */
        environment.execute();
    }

}
