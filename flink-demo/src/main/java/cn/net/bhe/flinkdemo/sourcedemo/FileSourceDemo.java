package cn.net.bhe.flinkdemo.sourcedemo;

import cn.net.bhe.mutil.As;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.net.URL;

public class FileSourceDemo {

    public static void main(String[] args) throws Exception {
        new FileSourceDemo().wordCount();
    }

    public void wordCount() throws Exception {
        /* 创建执行环境 */
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        environment.setParallelism(1);

        /* 数据入口 */
        URL url = this.getClass().getResource(FileSourceDemo.class.getSimpleName() + ".txt");
        As.isTrue(url != null);
        String path = url.getFile();
        DataStream<String> dataStream = environment.readTextFile(path);

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
