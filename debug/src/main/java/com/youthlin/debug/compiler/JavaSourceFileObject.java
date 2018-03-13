package com.youthlin.debug.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * 创建: youthlin.chen
 * 时间: 2018-03-13 19:29
 */
public class JavaSourceFileObject extends SimpleJavaFileObject {
    private final String code;

    public JavaSourceFileObject(File file) throws IOException {
        super(file.toURI(), Kind.SOURCE);
        FileInputStream in = new FileInputStream(file);
        int len = in.available();
        byte[] content = new byte[len];
        in.read(content);
        code = new String(content);
    }

    public JavaSourceFileObject(String fileName, String code) {
        super(URI.create("string:///" + fileName.replace(".", "/") + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return code;
    }
}
