package cn.net.bhe.hdfsmapreddemo.combiner;

import cn.net.bhe.hdfsmapreddemo._quickstart.AppStatReducer;
import cn.net.bhe.hdfsmapreddemo._quickstart._QuickStartDemo;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;

public class _CombinerDemo extends Reducer<Text, IntWritable, Text, IntWritable> {

    public static void main(String[] args) throws Exception {
        /* 获取Job */
        Job job = _QuickStartDemo.getJob(args);
        job.setJarByClass(_CombinerDemo.class);
        // 自定义Combiner
        // 观察日志输出
        // Map-Reduce Framework
        //     Combine input records=5000000
        //     Combine output records=80
        job.setCombinerClass(AppStatReducer.class);

        /* 提交Job */
        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);
    }
}
