package cn.net.bhe.hdfsclientdemo;

import cn.net.bhe.mutil.StrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class FsHelper {

    private static FileSystem fileSystem;
    private static final Object FS_LOCK = new Object();

    public static FileSystem getFs() throws Exception {
        if (fileSystem != null) {
            return fileSystem;
        }
        synchronized (FS_LOCK) {
            if (fileSystem != null) {
                return fileSystem;
            }
            Properties properties = new Properties();
            properties.load(FsServiceImpl.class.getClassLoader().getResourceAsStream("application.properties"));
            String uri = properties.getProperty("fs.uri");
            String user = properties.getProperty("fs.user");
            Configuration configuration = new Configuration();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                configuration.set(StrUtils.toString(entry.getKey()), StrUtils.toString(entry.getValue()));
            }
            return fileSystem = FileSystem.get(new URI(uri), configuration, user);
        }
    }

    public static void closeFs() {
        if (fileSystem != null) {
            try {
                fileSystem.close();
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

}
