package cn.net.bhe.jdkdemo.serializedemo;

import cn.net.bhe.mutil.FlUtils;
import cn.net.bhe.mutil.NmUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class _ObjectOutputStreamApp {

    public static void main(String[] args) throws IOException {
        User user = new User();
        user.setName(NmUtils.randomName());
        user.setAge(18);
        String path = FlUtils.getRootTmpWithPkg(_ObjectOutputStreamApp.class);
        FlUtils.mkdir(path);
        path = path + File.separator + user.getClass().getSimpleName();
        try (FileOutputStream fos = new FileOutputStream(path)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(user);
        }
    }

}
