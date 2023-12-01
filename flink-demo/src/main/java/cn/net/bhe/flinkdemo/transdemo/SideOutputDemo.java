package cn.net.bhe.flinkdemo.transdemo;

import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class SideOutputDemo {

    public static void main(String[] args) throws Exception {
        new SideOutputDemo().distinctByLength();
    }

    public void distinctByLength() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        // 分流
        OutputTag<String> shortTag = new OutputTag<String>("short") {
        };
        OutputTag<String> longTag = new OutputTag<String>("long") {
        };
        SingleOutputStreamOperator<String> singleDataStream = dataStream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) {
                int len = 5;
                if (value.length() < len) {
                    ctx.output(shortTag, value);
                } else {
                    ctx.output(longTag, value);
                }
            }
        });
        DataStream<String> shortStream = singleDataStream.getSideOutput(shortTag);
        DataStream<String> longStream = singleDataStream.getSideOutput(longTag);
        // 合流
        ConnectedStreams<String, String> connectedStreams = shortStream.connect(longStream);
        DataStream<String> coMapStream = connectedStreams.map(new CoMapImpl());

        /* 数据出口 */
        shortStream.print(shortTag.getId());
        longStream.print(longTag.getId());
        coMapStream.print();

        /* 执行 */
        environment.execute();
    }

    public static class CoMapImpl implements CoMapFunction<String, String, String> {

        @Override
        public String map1(String value) {
            return value;
        }

        @Override
        public String map2(String value) {
            return value;
        }
    }

}
