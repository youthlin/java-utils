# mail sender
Mail Sender / Java / SMTP / Fluent  

```
new MailSender()
     .start(new MailSender.SessionBuilder()
         .host("host")
         .auth("username", "password")
         .ssl(465)   // JDK 7 OK. JDK8 由于安全原因需要替换俩 jar 包
         .debug()
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
     .send();//发送
```