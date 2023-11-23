package cn.net.bhe.hdfsmapreddemo.capacityqueue;

import cn.net.bhe.hdfsmapreddemo._quickstart._QuickStartDemo;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;

public class _CapacityQueueDemo extends Reducer<Text, IntWritable, Text, IntWritable> {

    public static void main(String[] args) throws Exception {
        /* 获取Job */
        Job job = _QuickStartDemo.getJob(args);
        job.setJarByClass(_CapacityQueueDemo.class);
        Configuration configuration = job.getConfiguration();
        // 指定队列
        configuration.set("mapreduce.job.queuename", "hive");
        /* 提交Job */
        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);
    }
}
