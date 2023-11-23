package cn.net.bhe.hdfsmapreddemo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class _WordCountDemo extends Reducer<Text, IntWritable, Text, IntWritable> {

    /**
     * 入参：C:/Users/Administrator/Desktop/wordcount/input C:/Users/Administrator/Desktop/wordcount/output。
     */
    public static void main(String[] args) throws Exception {
        /* 获取Job */
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);

        /* 设置Jar包路径 */
        job.setJarByClass(_WordCountDemo.class);

        /* 关联Mapper和Reducer */
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);

        /* 设置map输出的KV类型 */
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        /* 设置最终输出KV类型 */
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        /* 提交Job */
        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);
    }
}
