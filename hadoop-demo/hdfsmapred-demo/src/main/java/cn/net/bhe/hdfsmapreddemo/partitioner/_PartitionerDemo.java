package cn.net.bhe.hdfsmapreddemo.partitioner;

import cn.net.bhe.hdfsmapreddemo._quickstart._QuickStartDemo;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;

public class _PartitionerDemo extends Reducer<Text, IntWritable, Text, IntWritable> {

    public static void main(String[] args) throws Exception {
        /* 获取Job */
        Job job = _QuickStartDemo.getJob(args);
        job.setJarByClass(_PartitionerDemo.class);
        // 自定义分区实现
        // 执行完毕后，查看输出文件个数。
        job.setPartitionerClass(MyPartitioner.class);
        job.setNumReduceTasks(10);
        /* 提交Job */
        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);
    }
}
