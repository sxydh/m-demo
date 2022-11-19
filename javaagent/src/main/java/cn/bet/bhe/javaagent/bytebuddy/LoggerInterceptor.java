package cn.bet.bhe.javaagent.bytebuddy;

import net.bytebuddy.implementation.bind.annotation.*;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * @author Administrator
 */
public class LoggerInterceptor {

    @RuntimeType
    public static Object log(@This Object zhis,
                             @SuperCall Callable<?> zuper,
                             @Origin String origin,
                             @AllArguments Object... args) throws Exception {
        Object obj = null;
        try {
            obj = zuper.call();
            return obj;
        } finally {
            System.out.println(LoggerInterceptor.class + " => @This => " + zhis);
            System.out.println(LoggerInterceptor.class + " => @SuperCall => " + obj);
            System.out.println(LoggerInterceptor.class + " => @Origin => " + origin);
            System.out.println(LoggerInterceptor.class + " => @AllArguments => " + Arrays.toString(args));
            System.out.println();
        }
    }

}
