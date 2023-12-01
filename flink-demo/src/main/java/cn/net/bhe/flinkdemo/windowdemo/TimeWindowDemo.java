package cn.net.bhe.flinkdemo.windowdemo;

import cn.net.bhe.mutil.StrUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class TimeWindowDemo {

    public static void main(String[] args) throws Exception {
        new TimeWindowDemo().wordConcat();
    }

    public void wordConcat() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        // 拼接10秒内的单词列表
        // 两种方式：增量聚合、全窗口聚合。

        OutputTag<String> aggTag = new OutputTag<String>("增量聚合") {
        };
        OutputTag<String> windowTag = new OutputTag<String>("全窗口聚合") {
        };
        SingleOutputStreamOperator<String> singleDataStream = dataStream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) {
                ctx.output(aggTag, value);
                ctx.output(windowTag, value);
            }
        });

        // 增量聚合
        // 窗口内的数据来一次处理一次
        DataStream<String> aggDataStream = singleDataStream.getSideOutput(aggTag);
        DataStream<String> aggResult = aggDataStream
                .keyBy(value -> StrUtils.EMPTY)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .aggregate(new WordConcatFunction());

        // 全量聚合
        // 窗口内的数据到齐后再处理
        DataStream<String> windowDataStream = singleDataStream.getSideOutput(windowTag);
        SingleOutputStreamOperator<String> applyResult = windowDataStream
                .keyBy(value -> StrUtils.EMPTY)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .apply(new WindowFunction<String, String, String, TimeWindow>() {
                    @Override
                    public void apply(String key, TimeWindow window, Iterable<String> input, Collector<String> out) {
                        System.out.println("WindowFunction#apply - " + key);
                        StringBuilder ret = new StringBuilder();
                        for (String e : input) {
                            ret.append(", ").append(e);
                        }
                        out.collect(ret.toString());
                    }
                });

        /* 数据出口 */
        aggResult.print(aggTag.getId());
        applyResult.print(windowTag.getId());

        /* 执行 */
        environment.execute();
    }

}
