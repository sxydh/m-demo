package cn.net.bhe.hdfsmapreddemo.outputformat;

import cn.net.bhe.hdfsmapreddemo._quickstart._QuickStartDemo;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;

public class _OutputFormatDemo extends Reducer<Text, IntWritable, Text, IntWritable> {

    public static void main(String[] args) throws Exception {
        /* 获取Job */
        Job job = _QuickStartDemo.getJob(args);
        job.setJarByClass(_OutputFormatDemo.class);
        // 自定义OutputFormat
        // 虽然自定义了OutputFormat，但是其继承自FileOutputFormat，FileOutputFormat要输出一个_SUCCESS文件，所以FileOutputFormat.setOutputPath并不能省略。
        job.setOutputFormatClass(MyFileOutputFormat.class);
        /* 提交Job */
        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);
    }
}
