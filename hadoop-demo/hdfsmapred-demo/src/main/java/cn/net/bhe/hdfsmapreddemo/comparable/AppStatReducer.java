package cn.net.bhe.hdfsmapreddemo.comparable;

import cn.net.bhe.hdfsmapreddemo._quickstart.AppLog;
import org.apache.hadoop.mapreduce.Reducer;

public class AppStatReducer extends Reducer<MyKey, AppLog, MyKey, AppLog> {

    @Override
    protected void reduce(MyKey key, Iterable<AppLog> values, Reducer<MyKey, AppLog, MyKey, AppLog>.Context context) {
        try {
            long length = 0;
            for (AppLog appLog : values) {
                length += appLog.getLength();
            }
            AppLog appLog = new AppLog()
                    .setAppId(key.toString())
                    .setLength(length / 1000 / 60);
            context.write(key, appLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
