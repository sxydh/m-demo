package cn.net.bhe.hdfsmapreddemo.compress;

import cn.net.bhe.hdfsmapreddemo._quickstart._QuickStartDemo;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.Lz4Codec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class _CompressDemo extends Reducer<Text, IntWritable, Text, IntWritable> {

    public static void main(String[] args) throws Exception {
        /* 获取Job */
        Job job = _QuickStartDemo.getJob(args);
        job.setJarByClass(_CompressDemo.class);
        Configuration configuration = job.getConfiguration();
        // 开启Mapper端输出压缩
        configuration.setBoolean("mapreduce.map.output.compress", true);
        configuration.setClass("mapreduce.map.output.compress.codec", Lz4Codec.class, CompressionCodec.class);
        // 开启Reducer端输出压缩
        FileOutputFormat.setCompressOutput(job, true);
        FileOutputFormat.setOutputCompressorClass(job, BZip2Codec.class);
        /* 提交Job */
        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);
    }
}
