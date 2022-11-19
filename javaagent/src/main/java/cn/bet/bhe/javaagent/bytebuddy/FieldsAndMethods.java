package cn.bet.bhe.javaagent.bytebuddy;


import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.util.UUID;

/**
 * Fields and methods
 *
 * @author Administrator
 * @see <a href="https://bytebuddy.net/#/tutorial">Fields and methods</a>
 */
public class FieldsAndMethods {

    /**
     * 委托方法调用
     */
    public void intercept() throws Exception {
        //noinspection resource
        DynamicType.Unloaded<Source> dynamicType = new ByteBuddy()
                .subclass(Source.class)
                .method(ElementMatchers.named("hello")).intercept(MethodDelegation.to(Target.class))
                .make();
        dynamicType.saveIn(new File("C:\\Users\\Administrator\\Desktop\\intercept"));
        dynamicType
                .load(getClass().getClassLoader())
                .getLoaded()
                .getDeclaredConstructor().newInstance()
                .hello("World");
    }

    /**
     * 委托方法调用@Annotation
     */
    public void interceptWithAnnotation() throws Exception {
        //noinspection resource
        DynamicType.Unloaded<MemoryDatabase> dynamicType = new ByteBuddy()
                .subclass(MemoryDatabase.class)
                .method(ElementMatchers.named("load")).intercept(MethodDelegation.to(LoggerInterceptor.class))
                .make();
        dynamicType.saveIn(new File("C:\\Users\\Administrator\\Desktop\\interceptWithAnnotation"));
        MemoryDatabase loggingDatabase = dynamicType
                .load(getClass().getClassLoader())
                .getLoaded()
                .getDeclaredConstructor().newInstance();
        loggingDatabase.load(UUID.randomUUID().toString());
        loggingDatabase.load(UUID.randomUUID().toString());
        loggingDatabase.load(UUID.randomUUID().toString());
    }

}
