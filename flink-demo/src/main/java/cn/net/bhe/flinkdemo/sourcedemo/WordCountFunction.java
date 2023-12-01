package cn.net.bhe.flinkdemo.sourcedemo;

import cn.net.bhe.mutil.StrUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class WordCountFunction implements FlatMapFunction<String, Tuple2<String, Integer>> {

    @Override
    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
        String[] words = value.split(StrUtils.SPACE);
        for (String word : words) {
            out.collect(new Tuple2<>(word, 1));
        }
    }

}
