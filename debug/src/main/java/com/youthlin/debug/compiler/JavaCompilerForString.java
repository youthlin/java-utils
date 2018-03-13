package com.youthlin.debug.compiler;

import com.youthlin.debug.JavaClassExecutor;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 创建: youthlin.chen
 * 时间: 2018-03-13 19:14
 */
public class JavaCompilerForString {
    private static final byte[] EMPTY = new byte[0];
    private static JavaCompiler compiler;

    public static byte[] compile(String fileName, String javaSource) {
        return compile(fileName, javaSource, "", null);
    }

    public static byte[] compile(String fileName, String javaSource, String classPath) {
        return compile(fileName, javaSource, classPath, null);
    }

    public static byte[] compile(String fileName, String javaSource, StringWriter out) {
        return compile(fileName, javaSource, "", out);
    }

    public static byte[] compile(String fileName, String javaSource, String classPath, StringWriter out) {
        JavaCompiler javaCompiler = getCompiler();
        StandardJavaFileManager standardFileManager = javaCompiler.getStandardFileManager(null, null, null);
        JavaSourceFileObject javaSourceFileObject = new JavaSourceFileObject(fileName, javaSource);
        ClassFileManager fileManager = new ClassFileManager(standardFileManager);
        List<String> options = null;
        if (classPath != null && !classPath.isEmpty()) {
            options = Arrays.asList("-cp", classPath);
        }
        JavaCompiler.CompilationTask task = javaCompiler.getTask(out, fileManager, null,
                options,
                null, Collections.singleton(javaSourceFileObject));
        Boolean call = task.call();
        if (call) {
            return fileManager.getClassBytes();
        }
        return EMPTY;
    }

    public static boolean supportCompiler() {
        return getCompiler() != null;
    }

    public static JavaCompiler getCompiler() {
        if (compiler == null) {
            compiler = ToolProvider.getSystemJavaCompiler();
        }
        return compiler;
    }

    public static void main(String[] args) {
        StringWriter out = new StringWriter();
        byte[] bytes = JavaCompilerForString.compile("Test",
                "import com.youthlin.debug.*;\n"
                        + "import java.util.Arrays;\n"
                        + "public class Test{\n"
                        + "  public static void main(String[] args){\n"
                        + "      HackSystem.out.println(Arrays.toString(args));\n"
                        + "      System.out.println(\"测试TEST你好\");\n"
                        + "  }\n"
                        + "}\n", out);
        if (bytes.length == 0) {
            System.out.println(out.toString());
        } else {
            String result = JavaClassExecutor.execute(bytes);
            System.out.println(result);
        }
    }
}
