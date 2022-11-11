package cn.net.bhe.h5frsserver.util;

import java.util.*;

/**
 * @author Administrator
 */
public class CollUtils {

    public static HashMap<String, Object> map(Object... kvs) {
        HashMap<String, Object> ret = new HashMap<>(16);
        Queue<Object> qkvs = new LinkedList<>(Arrays.asList(Optional.ofNullable(kvs).orElse(new String[0])));
        while (!qkvs.isEmpty()) {
            String k = Objects.toString(qkvs.poll(), null);
            Object v = qkvs.poll();
            ret.put(k, v);
        }
        return ret;
    }

}
