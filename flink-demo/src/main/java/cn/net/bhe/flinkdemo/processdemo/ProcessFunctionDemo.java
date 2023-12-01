package cn.net.bhe.flinkdemo.processdemo;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class ProcessFunctionDemo {

    public static void main(String[] args) throws Exception {
        new ProcessFunctionDemo().wordCopy();
    }

    public void wordCopy() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        /* 数据入口 */
        // 需要在192.168.233.129启动Socket服务：nc -lk 10010。
        DataStream<String> dataStream = environment.socketTextStream("192.168.233.129", 10010);

        /* 数据处理 */
        // 复制输入流，生成两条数据流。
        OutputTag<String> copyTag = new OutputTag<String>("copy") {
        };
        SingleOutputStreamOperator<String> result = dataStream.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) {
                out.collect(value);
                ctx.output(copyTag, value);
            }
        });

        /* 数据出口 */
        result.print();
        result.getSideOutput(copyTag).print(copyTag.getId());

        /* 执行 */
        environment.execute();
    }

}
