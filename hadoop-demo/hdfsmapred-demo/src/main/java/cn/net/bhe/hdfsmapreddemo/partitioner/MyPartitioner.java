package cn.net.bhe.hdfsmapreddemo.partitioner;

import cn.net.bhe.hdfsmapreddemo._quickstart.AppLog;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

import java.util.Random;

public class MyPartitioner extends Partitioner<Text, AppLog> {

    private Random random = new Random();

    @Override
    public int getPartition(Text text, AppLog appLog, int numPartitions) {
        return random.nextInt(100) % numPartitions;
    }
}
