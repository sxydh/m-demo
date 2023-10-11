package cn.net.bhe;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void createIndex() {
        try {
            new App().createIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void createDoc() {
        try {
            new App().createDoc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void createDocByMulti() {
        try {
            new App().createDocByMulti();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void countDoc() {
        try {
            new App().countDoc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void getDoc() {
        try {
            new App().getDoc("af14b169-b667-46e8-911c-21219478839f0 ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void writeCsv() {
        try {
            BufferedWriter writer = new App().writeCsv(UUID.randomUUID().toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}