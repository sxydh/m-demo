package cn.net.bhe.jdkdemo.serializedemo;

import cn.net.bhe.mutil.FlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class _ObjectInputStreamApp {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = FlUtils.getRootTmpWithPkg(_ObjectOutputStreamApp.class);
        path = path + File.separator + User.class.getSimpleName();
        try (FileInputStream fis = new FileInputStream(path)) {
            ObjectInputStream ois = new ObjectInputStream(fis);
            User user = (User) ois.readObject();
            System.out.println(user);
        }
    }

}
