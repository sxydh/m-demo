package cn.net.bhe.hdfsmapreddemo;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private IntWritable outv = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        // 累加
        int count = 0;
        for (IntWritable value : values) {
            count += value.get();
        }
        outv.set(count);
        // 写出
        context.write(key, outv);
    }
}
