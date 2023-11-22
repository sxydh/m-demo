package cn.net.bhe.hdfsclientdemo;

import org.apache.hadoop.fs.FileStatus;

import java.util.List;

public interface FsService {

    boolean mkdir(String path) throws Exception;

    void upload(String from, String to) throws Exception;

    boolean rename(String from, String to) throws Exception;

    boolean delete(String path) throws Exception;

    List<FileStatus> list(String path) throws Exception;

    void download(String from, String to) throws Exception;

}
