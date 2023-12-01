package cn.net.bhe.flinkdemo.processdemo;

import cn.net.bhe.mutil.StrUtils;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class KeyedProcessFunctionDemo {

    public static void main(String[] args) throws Exception {
        new KeyedProcessFunctionDemo().wordIncreaseContinuously();
    }

    public void wordIncreaseContinuously() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        DataStream<String> result = dataStream
                // 使用keyBy对输入数据进行分区
                // 这里将所有数据化为一个分区，简化输入。
                .keyBy(value -> StrUtils.EMPTY)
                // 对分区内数据进行处理
                // 获取过去一段时间内内单词长度连续增大的列表
                .process(new KeyedProcessFunction<String, String, String>() {
                    // 上一个单词
                    private ValueState<String> lastWord;
                    // 计时器标识
                    private ValueState<Long> timer;
                    // 单词长度连续增长的列表
                    private ListState<String> incWords;

                    @Override
                    public void open(Configuration parameters) {
                        lastWord = getRuntimeContext().getState(new ValueStateDescriptor<>("lastWord", String.class));
                        timer = getRuntimeContext().getState(new ValueStateDescriptor<>("timer", Long.class));
                        incWords = getRuntimeContext().getListState(new ListStateDescriptor<>("incWords", String.class));
                    }

                    @Override
                    public void processElement(String value, KeyedProcessFunction<String, String, String>.Context ctx, Collector<String> out) throws Exception {
                        if (StrUtils.isNotEmpty(lastWord.value()) && StrUtils.compareTo(value, lastWord.value()) > 0) {
                            incWords.add(value);
                        } else {
                            if (timer.value() != null) {
                                ctx.timerService().deleteProcessingTimeTimer(timer.value());
                            }
                            long newTimer = System.currentTimeMillis() + 10 * 1000;
                            ctx.timerService().registerProcessingTimeTimer(newTimer);
                            timer.update(newTimer);
                            incWords.clear();
                            incWords.add(value);
                        }
                        lastWord.update(value);
                    }


                    @Override
                    public void onTimer(long timestamp, KeyedProcessFunction<String, String, String>.OnTimerContext ctx, Collector<String> out) throws Exception {
                        out.collect(String.valueOf(timestamp));
                        for (String word : incWords.get()) {
                            out.collect(word);
                        }
                        out.collect(String.valueOf(timestamp));
                        ctx.timerService().deleteProcessingTimeTimer(timer.value());
                        timer.clear();
                        incWords.clear();
                    }

                    @Override
                    public void close() {
                        lastWord.clear();
                        timer.clear();
                        incWords.clear();
                    }
                });

        /* 数据出口 */
        result.print();

        /* 执行 */
        environment.execute();
    }

}
