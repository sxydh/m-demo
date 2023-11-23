package cn.net.bhe.hdfsmapreddemo.combinetextinputformat;

import cn.net.bhe.hdfsmapreddemo._quickstart._QuickStartDemo;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.CombineTextInputFormat;

public class _CombineTextInputFormatDemo extends Reducer<Text, IntWritable, Text, IntWritable> {

    public static void main(String[] args) throws Exception {
        /* 获取Job */
        Job job = _QuickStartDemo.getJob(args);
        job.setJarByClass(_CombineTextInputFormatDemo.class);
        // 指定FileInputFormat实现
        job.setInputFormatClass(CombineTextInputFormat.class);
        // 设置maxInputSplitSize=200MB
        // 观察日志输出（执行程序前，将输入文件复制多份）
        //     number of splits:11
        //         和_QuickStartDemo的number of splits对比
        CombineTextInputFormat.setMaxInputSplitSize(job, 200 * 1024 * 1024);
        /* 提交Job */
        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);
    }
}
