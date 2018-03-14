package com.youthlin.debug.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;

/**
 * 创建: youthlin.chen
 * 时间: 2018-03-13 19:36
 */
public class ClassFileManager extends ForwardingJavaFileManager {
    private JavaClassFileObject classFileObject;

    public ClassFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className,
            JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        classFileObject = new JavaClassFileObject(className, kind);
        return classFileObject;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                byte[] classBytes = classFileObject.getClassBytes();
                return super.defineClass(name, classBytes, 0, classBytes.length);
            }
        };
    }

    public byte[] getClassBytes() {
        return classFileObject.getClassBytes();
    }

}
