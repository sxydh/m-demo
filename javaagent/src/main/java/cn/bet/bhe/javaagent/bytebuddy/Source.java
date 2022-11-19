package cn.bet.bhe.javaagent.bytebuddy;

/**
 * @author Administrator
 */
public class Source {

    public String hello(String name) {
        String ret = this.getClass() + " => Hello " + name;
        System.out.println(ret);
        return ret;
    }

}
