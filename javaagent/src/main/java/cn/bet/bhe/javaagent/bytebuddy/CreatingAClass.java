package cn.bet.bhe.javaagent.bytebuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.io.File;


/**
 * 类创建
 *
 * @author Administrator
 * @see <a href="https://bytebuddy.net/#/tutorial">类创建</a>
 */
public class CreatingAClass {

    /**
     * 类创建
     */
    public void subclass() throws Exception {
        //noinspection resource
        DynamicType.Unloaded<?> dynamicType = new ByteBuddy()
                .subclass(Object.class)
                .name("example.Type")
                .make();
        dynamicType.saveIn(new File("C:\\Users\\Administrator\\Desktop\\subclass"));
    }

    /**
     * 类加载
     */
    public void load() {
        //noinspection resource
        Class<?> type = new ByteBuddy()
                .subclass(Object.class)
                .make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        System.out.println(type);
    }

}