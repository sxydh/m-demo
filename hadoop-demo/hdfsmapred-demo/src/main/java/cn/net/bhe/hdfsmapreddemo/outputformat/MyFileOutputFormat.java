package cn.net.bhe.hdfsmapreddemo.outputformat;

import cn.net.bhe.hdfsmapreddemo._quickstart.AppLog;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class MyFileOutputFormat extends FileOutputFormat<Text, AppLog> {

    @Override
    public RecordWriter<Text, AppLog> getRecordWriter(TaskAttemptContext job) {
        return new MyRecordWriter();
    }
}
