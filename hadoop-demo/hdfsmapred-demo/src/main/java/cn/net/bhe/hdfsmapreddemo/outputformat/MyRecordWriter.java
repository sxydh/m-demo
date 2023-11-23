package cn.net.bhe.hdfsmapreddemo.outputformat;

import cn.net.bhe.hdfsmapreddemo._quickstart.AppLog;
import cn.net.bhe.mutil.FlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

@Slf4j
public class MyRecordWriter extends RecordWriter<Text, AppLog> {

    @Override
    public void write(Text key, AppLog value) throws IOException, InterruptedException {
        try {
            FlUtils.writeToDesktop(System.lineSeparator() + key.toString() + value.toString(), _OutputFormatDemo.class.getName() + ".txt", Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void close(TaskAttemptContext context) {

    }
}
