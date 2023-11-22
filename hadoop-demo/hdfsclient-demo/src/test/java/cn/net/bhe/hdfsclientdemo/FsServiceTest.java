package cn.net.bhe.hdfsclientdemo;

import cn.net.bhe.mutil.StrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
class FsServiceTest {

    static FsService fsService;

    @BeforeAll
    static void init() {
        log.info(StrUtils.EMPTY);
        fsService = FsServiceImpl.getInst();
    }

    @Test
    void mkdir() throws Exception {
        boolean mkdir = fsService.mkdir("/sxydh");
        log.info(StrUtils.toString(mkdir));
    }

    @Test
    void upload() throws Exception {
        fsService.upload("C:/Users/Administrator/Desktop/a.txt", "/sxydh/tmp/a.txt");
    }

    @Test
    void rename() throws Exception {
        boolean rename = fsService.rename("/sxydh/tmp/a.txt", "/sxydh/tmp/a2.txt");
        log.info(StrUtils.toString(rename));
    }

    @Test
    void delete() throws Exception {
        boolean delete = fsService.delete("/sxydh/tmp/a2.txt");
        log.info(StrUtils.toString(delete));
    }

    @Test
    void list() throws Exception {
        List<FileStatus> list = fsService.list("/");
        for (FileStatus fileStatus : list) {
            log.info(fileStatus.getPath().toString());
        }
    }

    @Test
    void download() throws Exception {
        fsService.download("/sxydh/tmp/a.txt", "C:/Users/Administrator/Desktop/a.txt");
    }
}