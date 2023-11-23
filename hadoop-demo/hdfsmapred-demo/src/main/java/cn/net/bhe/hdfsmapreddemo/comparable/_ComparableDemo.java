package cn.net.bhe.hdfsmapreddemo.comparable;

import cn.net.bhe.hdfsmapreddemo._quickstart._QuickStartDemo;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * 自定义MapTask和ReduceTask输出数据的排序算法
 */
public class _ComparableDemo extends Reducer<Text, IntWritable, Text, IntWritable> {

    public static void main(String[] args) throws Exception {
        // 获取Job
        Job job = _QuickStartDemo.getJob(args);
        job.setMapperClass(AppStatMapper.class);
        job.setReducerClass(AppStatReducer.class);
        job.setMapOutputKeyClass(MyKey.class);
        job.setOutputKeyClass(MyKey.class);
        /* 提交Job */
        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);
    }
}
