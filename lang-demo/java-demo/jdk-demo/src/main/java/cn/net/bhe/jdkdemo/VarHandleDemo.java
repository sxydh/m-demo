package cn.net.bhe.jdkdemo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VarHandleDemo {

    private static final VarHandle VAR_HANDLE;

    static {
        try {
            VAR_HANDLE = MethodHandles.lookup().in(AnyClass.class)
                    .findVarHandle(AnyClass.class, "anyInt", int.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /************* Atomic Update *************/
    public void compareAndSet() {
        AnyClass anyClass = new AnyClass();
        VAR_HANDLE.compareAndSet(anyClass, 0, 1);
        System.out.println(anyClass.anyInt);
    }

    public static class AnyClass {
        public int anyInt = 0;
    }

}
