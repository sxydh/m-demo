package cn.net.bhe.flinkdemo.windowdemo;

import cn.net.bhe.mutil.StrUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

public class WatermarkDemo {

    public static void main(String[] args) throws Exception {
        new WatermarkDemo().timeWinWordCount();
    }

    public void timeWinWordCount() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        environment.setParallelism(1);

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        //
        // 输入示例
        //
        // 1699120793940
        // 1699120794940
        //
        // 1699120795940
        // 1699120796940
        // 1699120797940
        // 1699120798940
        // 1699120799940
        //
        // 1699120800940
        // 1699120801940
        // 1699120802940
        // 1699120803940
        // 1699120804940
        //
        // 1699120805940
        // 1699120806940
        // 1699120807940
        // 1699120808940
        // 1699120809940
        //
        // 1699120810940
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        // 以事件时间为准，5秒内单词个数。

        OutputTag<Tuple1<Long>> lateTag = new OutputTag<Tuple1<Long>>("late") {
        };
        SingleOutputStreamOperator<Tuple3<Long, Long, Long>> result = dataStream.map(new MapFunction<String, Tuple1<Long>>() {
                    @Override
                    public Tuple1<Long> map(String value) {
                        return new Tuple1<>(Long.parseLong(StrUtils.trim(value)));
                    }
                })
                // Watermark设置
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                // Watermark固定延迟
                                .<Tuple1<Long>>forBoundedOutOfOrderness(Duration.ofSeconds(0))
                                // Watermark获取方式
                                .withTimestampAssigner((event, timestamp) -> event.f0))
                .keyBy(value -> StrUtils.EMPTY)
                // 时间窗口大小
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                // 时间窗口延迟
                .allowedLateness(Time.seconds(10))
                // 迟到数据输出到侧流
                .sideOutputLateData(lateTag)
                // 时间窗口内单词计数
                .apply(new WindowFunction<Tuple1<Long>, Tuple3<Long, Long, Long>, String, TimeWindow>() {
                    @Override
                    public void apply(String key, TimeWindow window, Iterable<Tuple1<Long>> input, Collector<Tuple3<Long, Long, Long>> out) {
                        long size = 0;
                        for (Object ignored : input) {
                            size++;
                        }
                        out.collect(new Tuple3<>(size, window.getStart(), window.getEnd()));
                    }
                });

        /* 数据出口 */
        result.print();
        result.getSideOutput(lateTag).print("late");

        /* 执行 */
        environment.execute();
    }

}
