package com.youthlin.debug;

import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2018-03-11 23:05
 */
public class JavaClassExecuter {
    public static String execute(byte[] classByte) {
        HackSystem.clearBuffer();
        ClassModifier cm = new ClassModifier(classByte);
        byte[] modifiedBytes = cm.modifyUTF8Constant(System.class.getName().replace(".", "/"),
                HackSystem.class.getName().replace(".", "/"));
        HotSwapClassloader loader = new HotSwapClassloader();
        Class clazz = loader.loadByte(modifiedBytes);
        try {
            @SuppressWarnings("unchecked")
            Method method = clazz.getMethod("main", String[].class);
            method.invoke(null, (Object) new String[] { null });
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return HackSystem.getByfferString();
    }

    public static void main(String[] args) {
        System.out.println(System.class.getName().replace(".", "/"));
    }
}
