package com.youthlin.debug.web;

import com.youthlin.debug.compiler.JavaCompilerForString;
import com.youthlin.debug.execute.JavaClassExecutor;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

/**
 * 创建: youthlin.chen
 * 时间: 2018-03-25 20:46
 */
@MultipartConfig
public class DebugServlet extends HttpServlet {
    private static final long serialVersionUID = 1893866012670378385L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        writePage(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Part bytes = req.getPart("bytes");
        StringBuilder sb = new StringBuilder();
        String result = "";
        if (bytes != null) {
            InputStream in = bytes.getInputStream();
            int size = in.available();
            byte[] classBytes = new byte[size];
            int read = in.read(classBytes);
            if (size > 0 && read > 0) {
                result = JavaClassExecutor.execute(classBytes);
                sb.append(result).append("\n");
            }
        }
        String code = getParam(req, "code");
        String fileName = getParam(req, "fileName");
        String importClass = getParam(req, "importClass");
        if (code != null && !code.isEmpty()) {
            if (JavaCompilerForString.supportCompiler()) {
                Set<String> classpath = JavaClassExecutor.getClasspathSet();
                if (importClass != null && !importClass.isEmpty()) {
                    classpath.addAll(JavaClassExecutor.getClasspathSet(importClass.split("[,\\s]")));
                }
                StringWriter out = new StringWriter();
                byte[] classBytes = JavaCompilerForString
                        .compile(fileName, code, JavaClassExecutor.getClasspath(classpath), out);
                if (classBytes.length > 0) {
                    result = JavaClassExecutor.execute(classBytes);
                } else {
                    result = out.toString();
                }
            } else {
                result = "服务器不支持即时编译 请直接上传编译后的 class 文件";
            }
        }
        sb.append(result).append("\n");
        req.setAttribute("result", sb.toString());
        writePage(req, resp);
    }

    private void writePage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='zh-CN'>");
        out.println("<head>");
        out.println("    <meta charset='utf-8'>");
        out.println("    <meta http-equiv='X-UA-Compatible' content='IE=edge'>");
        out.println("    <meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("    <!-- 上述3个meta标签*必须*放在最前面，任何其他内容都*必须*跟随其后！ -->");
        out.println("    <title>Debug</title>");
        out.println("    <!-- Bootstrap -->");
        out.println("    <!-- 最新版本的 Bootstrap 核心 CSS 文件 -->");
        out.println("    <link rel='stylesheet' href='https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css'>");
        out.println("    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->");
        out.println("    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->");
        out.println("    <!--[if lt IE 9]>");
        out.println("    <script src='https://cdn.bootcss.com/html5shiv/3.7.3/html5shiv.min.js'></script>");
        out.println("    <script src='https://cdn.bootcss.com/respond.js/1.4.2/respond.min.js'></script>");
        out.println("    <![endif]-->");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println();
        out.println("    <form class='form-horizontal' action='' method='post' enctype='multipart/form-data'>");
        out.println("        ");
        out.println("        <div class='form-group'>");
        out.println("            <label for='bytes' class='col-sm-2 control-label'>Class 文件</label>");
        out.println("            <div class='col-sm-10'>");
        out.println("                <input class='form-control' id='bytes' type='file' name='bytes'>");
        out.println("            </div>");
        out.println("        </div>");
        out.println("        <hr>");
        if (JavaCompilerForString.supportCompiler()) {
            out.println("        <div class='form-group'>");
            out.println("            <label for='fileName' class='col-sm-2 control-label'>Java 类名</label>");
            out.println("            <div class='col-sm-10'>");
            out.println("<input class='form-control' id='fileName' type='text' name='fileName' value='"
                    + getParam(request, "fileName") + "'>");
            out.println("            </div>");
            out.println("        </div>");
            out.println("        <div class='form-group'>");
            out.println("            <label for='importClass' class='col-sm-2 control-label'>依赖的类</label>");
            out.println("            <div class='col-sm-10'>");
            out.println("<input class='form-control' id='importClass' type='text' name='importClass' value='"
                    + getParam(request, "importClass") + "'>");
            out.println("            </div>");
            out.println("        </div>");
            out.println("        ");
            out.println("        <div class='form-group'>");
            out.println("            <label for='code' class='col-sm-2 control-label'>Java 代码</label>");
            out.println("            <div class='col-sm-10'>");
            out.println("<textarea name='code' id='code' cols='30' rows='10' class='form-control'>"
                    + getParam(request, "code") + "</textarea>");
            out.println("            </div>");
            out.println("        </div>");
        }
        out.println("        <div class='form-group'>");
        out.println("            <div class='col-sm-offset-2 col-sm-10'>");
        out.println("                <button class='btn btn-primary'>提交</button>");
        out.println("            </div>");
        out.println("        </div>");
        out.println("    </form>");
        out.println();
        out.println("    ");
        out.println("<pre>");
        Object result = request.getAttribute("result");
        if (result instanceof String) {
            out.println(result);
        }
        out.println("</pre>");
        out.println();
        out.println("</div>");
        out.println();
        out.println();
        out.println("<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->");
        out.println("<script src='https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js'></script>");
        out.println("<!-- Include all compiled plugins (below), or include individual files as needed -->");
        out.println("<script src='https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js'></script>");
        out.println("</body>");
        out.println("</html>");
    }

    private String getParam(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        return value == null ? "" : value;
    }

}
