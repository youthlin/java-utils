# Java Utils

1. SMTP Mail Sender

   ```
   new MailSender()
        .start(new MailSender.SessionBuilder()
            .host("host")
            .auth("username", "password")
            .ssl(465)   // JDK 7 OK. JDK8 由于安全原因需要替换俩 jar 包
            .debug()    // 注释这行关闭调试信息的输出
            .toSession()
        )//Session 只能调用一次 start
        .from("email", "DisplayName")//发件人 email,name
        .to("to", "DisplayName")//收件人
        .cc("cc")//抄送
        .cc("another", "name")
        .bcc("bcc")//密送
        .subject("subject")//主题
        .text("content")//只能设置一次内容. 或[html("html content")][content("plain",false)][content("html",true);]
        .attachment("path/to/file", "cid")//内嵌附件
        .attachment("path/to/file")//普通附件
        .attachment(file)//普通附件
        .dkim(new File("D:/key.der"), "youthlin.com", "xxx.youthlin")//验证发信人身份
        .send();//发送
        // DKIM 用于验证发信人身份，降低被判垃圾邮件的概率，用法见代码注释
        // 发给 Gmail 的邮件中附件类型严格受限，参加: https://support.google.com/mail/answer/6590
   ```
2. I18N
    * <code>__("str");</code>
    * <code>_x("str","context");</code>
    * <code>_n("single","plural",n);</code>
    * <code>_nx("single","plural",n,"context");</code>
    * <code>_n("One Apple", "{0} Apples", n, param)</code>
