package cn.bet.bhe.javaagent.bytebuddy;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Administrator
 */
public class LoggerInterceptor {

    public static List<String> log(@This Object zhis,
                                   @SuperCall Callable<List<String>> zuper,
                                   @Origin String origin,
                                   @AllArguments String... args) throws Exception {
        List<String> list = null;
        try {
            list = zuper.call();
            return list;
        } finally {
            System.out.println(LoggerInterceptor.class + " => @This => " + zhis);
            System.out.println(LoggerInterceptor.class + " => @SuperCall => " + list);
            System.out.println(LoggerInterceptor.class + " => @Origin => " + origin);
            System.out.println(LoggerInterceptor.class + " => @AllArguments => " + Arrays.toString(args));
            System.out.println();
        }
    }

}
