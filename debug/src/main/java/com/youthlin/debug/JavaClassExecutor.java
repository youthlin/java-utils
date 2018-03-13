package com.youthlin.debug;

import java.io.File;
import java.lang.reflect.Method;

/**
 * 创建: youthlin.chen
 * 时间: 2018-03-11 23:05
 */
public class JavaClassExecutor {
    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static String execute(byte[] classByte) {
        HackSystem.clearBuffer();
        try {
            ClassModifier cm = new ClassModifier(classByte);
            byte[] modifiedBytes = cm.modifyUTF8Constant(System.class.getName().replace(".", "/"),
                    HackSystem.class.getName().replace(".", "/"));
            HotSwapClassloader loader = new HotSwapClassloader();
            Class clazz = loader.loadByte(modifiedBytes);
            @SuppressWarnings("unchecked")
            Method method = clazz.getMethod("main", String[].class);
            method.invoke(null, (Object) null);
        } catch (Throwable e) {
            e.printStackTrace(HackSystem.out);
            throw new ExecuteException(e);
        }
        return HackSystem.getByfferString();
    }

    public static String getClasspath() {
        String jarPath = JavaClassExecutor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        jarPath = jarPath.substring(0, jarPath.lastIndexOf("/"));
        File dir = new File(jarPath);
        if (!dir.exists()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        listFile(result, dir);
        return result.toString();
    }

    public static String getPathSeparator() {
        return PATH_SEPARATOR;
    }

    private static void listFile(StringBuilder result, File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;

            for (File f : files) {
                listFile(result, f);
            }
        } else {
            result.append(file.getAbsolutePath()).append(PATH_SEPARATOR);
        }
    }

    public static void main(String[] args) {
        System.out.println(System.class.getName().replace(".", "/"));
    }

}
