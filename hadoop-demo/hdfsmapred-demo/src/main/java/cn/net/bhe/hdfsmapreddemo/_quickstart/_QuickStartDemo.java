package cn.net.bhe.hdfsmapreddemo._quickstart;

import cn.net.bhe.mutil.ArrUtils;
import cn.net.bhe.mutil.As;
import cn.net.bhe.mutil.DtUtils;
import cn.net.bhe.mutil.FlUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Random;

public class _QuickStartDemo {

    public static void main(String[] args) throws Exception {
        /* 获取Job */
        Job job = getJob(args);
        /* 提交Job */
        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);
    }

    public static Job getJob(String[] args) throws Exception {
        /* 获取Job */
        Configuration configuration = new Configuration();
        Job job = Job.getInstance(configuration);

        /* 设置Jar包路径 */
        job.setJarByClass(_QuickStartDemo.class);

        /* 关联Mapper和Reducer */
        job.setMapperClass(AppStatMapper.class);
        job.setReducerClass(AppStatReducer.class);

        /* 设置map输出的KV类型 */
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(AppLog.class);

        /* 设置最终输出KV类型 */
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(AppLog.class);

        /* 设置输入输出路径 */
        String basePath = FlUtils.getDesktop() + File.separator + "quickstart";
        String inputPath = ArrUtils.firstNotNull(ArrUtils.at(args, 0), basePath + File.separator + "input");
        initInput(inputPath);
        String outputPath = ArrUtils.firstNotNull(ArrUtils.at(args, 1), basePath + File.separator + "output");
        FileInputFormat.setInputPaths(job, new Path(inputPath));
        initOutput(outputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job;
    }

    public static void initOutput(String path) {
        File file = new File(path);
        if (file.exists()) {
            As.isTrue(FlUtils.delete(file));
        }
    }

    public static void initInput(String path) throws Exception {
        As.isTrue(FlUtils.mkdir(path));
        File file = new File(path + File.separator + "app_log.txt");
        if (file.exists()) {
            As.isTrue(file.delete());
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        String[] appIds = new String[]{
                "Tik Tok", "Alipay", "JD", "BiliBili", "QQ", "WeChat", "Gaode Map", "DingTalk", "Meituan", "PDD"};
        Random random = new Random();
        while (file.length() < 2.8 * 1024 * 1024) {
            String appId = appIds[random.nextInt(appIds.length)];
            Date startDate = DtUtils.addDays(DtUtils.date(), -random.nextInt(30));
            Date quitDate = DtUtils.addMinutes(startDate, random.nextInt(120));
            writer.write(appId + "," + DtUtils.format(startDate) + "," + DtUtils.format(quitDate));
            writer.newLine();
        }
        writer.close();
    }

}
