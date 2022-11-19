package cn.bet.bhe.javaagent.bytebuddy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class MemoryDatabase {

    private final List<String> list = new ArrayList<>();

    public List<String> load(String ele) {
        list.add(ele);
        return new ArrayList<>(list);
    }

}
