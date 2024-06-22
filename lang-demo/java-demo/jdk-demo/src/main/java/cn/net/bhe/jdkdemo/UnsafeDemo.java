package cn.net.bhe.jdkdemo;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeDemo {

    private static final Unsafe UNSAFE;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /************ 内存操作 ************/
    public void allocateMemory() {
        long address = UNSAFE.allocateMemory(4);
        UNSAFE.putInt(address, 1);
        System.out.println(UNSAFE.getInt(address));
        UNSAFE.freeMemory(address);
    }

    public void allocateInstance() throws Exception {
        AnyClass anyClass = (AnyClass) UNSAFE.allocateInstance(AnyClass.class);
        System.out.println(anyClass.anyInt);
    }

    /************ 内存写入 ************/
    public void putInt() throws Exception {
        AnyClass anyClass = new AnyClass();
        Field field = anyClass.getClass().getDeclaredField("anyInt");
        UNSAFE.putInt(anyClass, UNSAFE.objectFieldOffset(field), 1);
        System.out.println(anyClass.anyInt);
    }

    public void compareAndSwapLong() throws Exception {
        AnyClass anyClass = new AnyClass();
        Field field = anyClass.getClass().getDeclaredField("anyInt");
        UNSAFE.compareAndSwapInt(anyClass, UNSAFE.objectFieldOffset(field), 0, 1);
        System.out.println(anyClass.anyInt);
    }

    /************ 线程操作 ************/
    public void park() {
        System.out.println(System.currentTimeMillis());
        // isAbsolute 表示是否是绝对时间戳
        UNSAFE.park(false, 1000000000L);
        System.out.println(System.currentTimeMillis());
    }

    public void unpark() throws Exception {
        Thread thread = new Thread(() -> {
            System.out.println(System.currentTimeMillis());
            UNSAFE.park(false, 3000000000L);
            System.out.println(System.currentTimeMillis());
        });
        thread.start();
        Thread.sleep(1000);
        UNSAFE.unpark(thread);
    }

    public static class AnyClass {
        public int anyInt = 0;
    }

}
