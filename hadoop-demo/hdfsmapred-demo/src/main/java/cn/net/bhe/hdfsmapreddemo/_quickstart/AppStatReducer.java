package cn.net.bhe.hdfsmapreddemo._quickstart;

import cn.net.bhe.mutil.StrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

@Slf4j
public class AppStatReducer extends Reducer<Text, AppLog, Text, AppLog> {

    @Override
    protected void reduce(Text key, Iterable<AppLog> values, Reducer<Text, AppLog, Text, AppLog>.Context context) {
        try {
            long length = 0;
            for (AppLog appLog : values) {
                length += appLog.getLength();
            }
            AppLog appLog = new AppLog()
                    .setAppId(key.toString())
                    .setStartDate(StrUtils.EMPTY)
                    .setQuitDate(StrUtils.EMPTY)
                    .setLength(length / 1000 / 60);
            context.write(key, appLog);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
}
