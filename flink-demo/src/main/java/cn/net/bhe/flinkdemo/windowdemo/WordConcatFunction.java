package cn.net.bhe.flinkdemo.windowdemo;

import cn.net.bhe.mutil.StrUtils;
import org.apache.flink.api.common.functions.AggregateFunction;

public class WordConcatFunction implements AggregateFunction<String, String, String> {
    @Override
    public String createAccumulator() {
        return StrUtils.EMPTY;
    }

    @Override
    public String add(String value, String accumulator) {
        System.out.println("WordConcatFunction#add - " + value);
        return accumulator + ", " + value;
    }

    @Override
    public String getResult(String accumulator) {
        System.out.println("WordConcatFunction#getResult - " + accumulator);
        return accumulator;
    }

    @Override
    public String merge(String a, String b) {
        System.out.println("WordConcatFunction#merge - " + a + ", " + b);
        return a + ", " + b;
    }
}
