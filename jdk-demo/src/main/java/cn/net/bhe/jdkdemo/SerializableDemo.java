package cn.net.bhe.jdkdemo;

import cn.net.bhe.mutil.As;
import cn.net.bhe.mutil.FlUtils;
import cn.net.bhe.mutil.StrUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.*;
import java.util.Random;

@Data
@Accessors(chain = true)
public class SerializableDemo implements Serializable {

    private Byte byte_;
    private Boolean boolean_;
    private Character char_;
    private Short short_;
    private Integer int_;
    private Float float_;
    private Double double_;
    private Long long_;
    private String string;

    private String getPath() {
        String path = FlUtils.getRootTmp();
        As.isTrue(FlUtils.mkdir(path));
        return path + File.separator + SerializableDemo.class.getSimpleName();
    }

    public void write() throws Exception {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(getPath()));
        Random random = new Random();
        objectOutputStream.writeObject(new SerializableDemo()
                .setByte_((byte) random.nextInt(127))
                .setBoolean_(random.nextBoolean())
                .setChar_((char) random.nextInt(255))
                .setShort_((short) random.nextInt(255))
                .setInt_(random.nextInt())
                .setFloat_(random.nextFloat())
                .setDouble_(random.nextDouble())
                .setLong_(random.nextLong())
                .setString(StrUtils.randomChs(5)));
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    public SerializableDemo read() throws Exception {
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(getPath()));
        return (SerializableDemo) objectInputStream.readObject();
    }

}
