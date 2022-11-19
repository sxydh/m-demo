package cn.bet.bhe.javaagent.bytebuddy;

/**
 * @author Administrator
 */
public class Target {

    public static String hello(String name) {
        String ret = Target.class + " => Hello " + name;
        System.out.println(ret);
        return ret;
    }

}
