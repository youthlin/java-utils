package com.youthlin.debug.execute;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创建: youthlin.chen
 * 时间: 2018-03-11 23:05
 */
public class JavaClassExecutor {
    private static final String PATH_SEPARATOR = System.getProperty("path.separator");
    private static final String[] EMPTY_ARR = new String[0];

    /**
     * 执行给定的 class 类的 main 方法
     * 首先将类里 {@link System} 替换为{@link HackSystem}
     * 以拦截所有往 {@link System#out} 输出的内容, 重定向到 {@link HackSystem#out}
     * 然后 invoke main 方法, 执行给定的类
     *
     * @return invoke 无异常, 返回程序中往 {@link System#out} 输出的内容; invoke 异常时, 返回异常堆栈
     * @see Exception#printStackTrace(java.io.PrintStream)
     */
    public static String execute(byte[] classByte) {
        HackSystem.clearBuffer();
        try {
            Class clazz = hackSout(classByte);
            @SuppressWarnings("unchecked")
            Method method = clazz.getMethod("main", String[].class);
            method.invoke(null, (Object) null);
        } catch (Throwable e) {
            e.printStackTrace(HackSystem.out);
            // throw new HackException(e);
        }
        return HackSystem.getOutput();
    }

    /**
     * 替换 {@link System} 为 {@link HackSystem}
     */
    private static Class hackSout(byte[] classByte) {
        try {
            ClassModifier cm = new ClassModifier(classByte);
            byte[] modifiedBytes = cm.modifyUTF8Constant(System.class.getName().replace(".", "/"),
                    HackSystem.class.getName().replace(".", "/"));
            return new HotSwapClassloader().loadByte(modifiedBytes);//每次 new 一个 classloader 不影响下次 load 同名的类
        } catch (Throwable t) {
            throw new HackException(t);
        }
    }

    //region getClasspath
    public static String getClasspath() {
        return getClasspath(JavaClassExecutor.class);
    }

    public static String getClasspath(Class<?> clazz) {
        Set<String> classpathSet = getClasspathSetByClass(clazz);
        return getClasspath(classpathSet);
    }

    public static String getClasspath(Set<String> classpathSet) {
        StringBuilder sb = new StringBuilder();
        for (String classpath : classpathSet) {
            if (sb.length() > 0) {
                sb.append(getPathSeparator());
            }
            sb.append(classpath);
        }
        return sb.toString();
    }

    public static Set<String> getClasspathSet() {
        return getClasspathSetByClass(JavaClassExecutor.class);
    }

    /**
     * 尝试用正则匹配出代码中的 import 语句, 解出依赖的类
     */
    public static Set<String> getClasspathSetByCode(String code) {
        Set<String> clazzNameSet = new HashSet<String>();
        Pattern pattern = Pattern.compile("import (.*?);");
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            String clazzName = matcher.group(1);
            if (!clazzName.contains("*")) {
                clazzNameSet.add(clazzName);
            }
        }
        String[] array = clazzNameSet.toArray(EMPTY_ARR);
        return getClasspathSetByClassName(array);
    }

    public static Set<String> getClasspathSetByClassName(String... classNames) {
        Set<String> result = new HashSet<String>();
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                result.addAll(getClasspathSetByClass(clazz));
            } catch (ClassNotFoundException ignore) {
            }
        }
        return result;
    }

    /**
     * 获取给定 Class 所在的 classpath 及其可能相关的 classpath
     */
    public static Set<String> getClasspathSetByClass(Class<?> clazz) {
        Set<String> result = new HashSet<String>();
        String jarPath = ".";
        try {
            result.add(clazz.getResource("/").getPath());
            jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        } catch (Exception ignore) {
        }
        File dir = new File(jarPath);
        if (!dir.exists()) {
            return result;
        }
        if (dir.isFile()) {
            dir = dir.getParentFile();
        }
        listFile(result, dir);//.jar files
        return result;
    }

    private static void listFile(Set<String> result, File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File f : files) {
                listFile(result, f);
            }
        } else {
            String fileName = file.getName();
            if (fileName.endsWith(".jar") || fileName.endsWith(".zip")) {
                result.add(file.getAbsolutePath());
            }
        }
    }
    //endregion

    public static String getPathSeparator() {
        return PATH_SEPARATOR;
    }

    public static void main(String[] args) {
        System.out.println(System.class.getName().replace(".", "/"));
    }

}
