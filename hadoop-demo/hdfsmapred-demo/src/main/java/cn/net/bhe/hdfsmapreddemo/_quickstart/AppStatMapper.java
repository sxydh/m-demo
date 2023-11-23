package cn.net.bhe.hdfsmapreddemo._quickstart;

import cn.net.bhe.mutil.DtUtils;
import cn.net.bhe.mutil.StrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

@Slf4j
public class AppStatMapper extends Mapper<LongWritable, Text, Text, AppLog> {

    private Text outk = new Text();

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, AppLog>.Context context) {
        try {
            String string = value.toString();
            if (StrUtils.isNotEmpty(string)) {
                String[] split = string.split(",");
                outk.set(split[0]);
                AppLog appLog = new AppLog()
                        .setAppId(split[0])
                        .setStartDate(split[1])
                        .setQuitDate(split[2]);
                appLog.setLength(DtUtils.parse(appLog.getQuitDate()).getTime() - DtUtils.parse(appLog.getStartDate()).getTime());
                context.write(outk, appLog);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
}
