package cn.net.bhe.hdfsclientdemo;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FsServiceImpl implements FsService {

    private static FsServiceImpl fsService;
    private static final Object INST_LOCK = new Object();

    private FileSystem fileSystem;

    private FsServiceImpl() {
    }

    public static FsService getInst() {
        if (fsService != null) {
            return fsService;
        }
        synchronized (INST_LOCK) {
            if (fsService != null) {
                return fsService;
            }
            try {
                fsService = new FsServiceImpl();
                fsService.fileSystem = FsHelper.getFs();
                return fsService;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean mkdir(String path) throws Exception {
        return fileSystem.mkdirs(new Path(path));
    }

    @Override
    public void upload(String from, String to) throws Exception {
        fileSystem.copyFromLocalFile(false, false, new Path(from), new Path(to));
    }

    @Override
    public boolean rename(String from, String to) throws Exception {
        return fileSystem.rename(new Path(from), new Path(to));
    }

    @Override
    public boolean delete(String path) throws Exception {
        return fileSystem.delete(new Path(path), true);
    }

    @Override
    public List<FileStatus> list(String path) throws Exception {
        RemoteIterator<LocatedFileStatus> iterator = fileSystem.listFiles(new Path(path), true);
        List<FileStatus> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    @Override
    public void download(String from, String to) throws Exception {
        fileSystem.copyToLocalFile(false, new Path(from), new Path(to), false);
    }
}
