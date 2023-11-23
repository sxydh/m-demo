package cn.net.bhe.hdfsmapreddemo.comparable;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@Data
@Accessors(chain = true)
public class MyKey implements WritableComparable<MyKey> {

    private String value;

    public void set(String value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(value);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        value = in.readUTF();
    }

    @Override
    public int compareTo(MyKey o) {
        return -this.value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
