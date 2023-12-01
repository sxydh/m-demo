package cn.net.bhe.erhlcdemo;

import cn.net.bhe.mutil.NmUtils;
import cn.net.bhe.mutil.Snowflake;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

@Slf4j
class ErhlcServiceTest {

    private static ErhlcService service;

    @BeforeAll
    static void beforeAll() {
        service = new ErhlcServiceImpl();
    }

    @Test
    void createIndex() throws Exception {
        boolean ret = service.createIndex("user");
        log.info(String.valueOf(ret));
    }

    @Test
    void getIndex() throws Exception {
        String[] ret = service.getIndex("user");
        log.info(Arrays.toString(ret));
    }

    @Test
    void delIndex() throws Exception {
        boolean ret = service.delIndex("user");
        log.info(String.valueOf(ret));
    }

    @Test
    void createDoc() throws Exception {
        ErhlcServiceImpl.User user = buildUser();
        String ret = service.createDoc("user", user.getId().toString(), JSON.toJSONString(user));
        log.info(ret);
    }

    private ErhlcServiceImpl.User buildUser() {
        Snowflake snowflake = new Snowflake();
        Random random = new Random();
        return new ErhlcServiceImpl.User()
                .setId(snowflake.nextId())
                .setName(NmUtils.randomName(5))
                .setAge(20 + random.nextInt(20))
                .setSex(new String[]{"Female", "Male"}[random.nextInt(2)]);
    }

    @Test
    void getDoc() throws Exception {
        String ret = service.getDoc("user", "10884545467953152");
        log.info(ret);
    }

    @Test
    void delDoc() throws Exception {
        String ret = service.delDoc("user", "10884545467953152");
        log.info(ret);
    }

    @Test
    void bulkDoc() throws Exception {
        int len = 100;
        String[] ids = new String[len];
        String[] docs = new String[len];
        for (int i = 0; i < len; i++) {
            ErhlcServiceImpl.User user = buildUser();
            ids[i] = String.valueOf(user.getId());
            docs[i] = JSON.toJSONString(user);
        }
        String[] ret = service.bulkDoc("user", ids, docs);
        log.info(Arrays.toString(ret));
    }

    @Test
    void delBulkDoc() throws Exception {
        String[] ret = service.delBulkDoc("user", new String[]{"10885056732639232", "10885056460009472"});
        log.info(Arrays.toString(ret));
    }

    @Test
    void query() throws Exception {
        String[] ret = service.query("user", "id", 0, 5);
        log.info(Arrays.toString(ret));
    }
}