package com.youthlin.debug.execute;

/**
 * 创建: youthlin.chen
 * 时间: 2018-03-11 22:25
 */
public class HotSwapClassloader extends ClassLoader {
    public HotSwapClassloader() {
        super(HotSwapClassloader.class.getClassLoader());
    }

    public Class loadByte(byte[] classBytes) {
        return defineClass(null, classBytes, 0, classBytes.length);
    }

}
