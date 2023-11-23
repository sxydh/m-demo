package cn.net.bhe.hdfsmapreddemo._quickstart;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@Data
@Accessors(chain = true)
public class AppLog implements Writable {

    private String appId;
    private String startDate;
    private String quitDate;
    private Long length;

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(appId);
        out.writeUTF(startDate);
        out.writeUTF(quitDate);
        out.writeLong(length);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        appId = in.readUTF();
        startDate = in.readUTF();
        quitDate = in.readUTF();
        length = in.readLong();
    }
}
