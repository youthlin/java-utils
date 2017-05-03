# Java Utils

1. SMTP Mail Sender

   ```
   MailSender
          .newInstance(MailSender.newSessionBuilder()
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
   使用 DKIM 验证发信人身份：
   使用 OpenSSL 生成密钥对, 或者在 http://dkimcore.org/tools/ 生成
   再把 Base64 文本格式的私钥转换为 der 二进制证书: openssl pkcs8 -topk8 -nocrypt -in key.pem -outform der -out key.der 其中 key.pem 是第一步生成的私钥, 以「-----BEGIN RSA PRIVATE KEY-----」开头的文件, key.der 是要保存的文件
   把公钥部署到域名的 TXT 记录中, 格式可参加步骤 1 网址.(记录名：xxx._domainkey, 记录值：p=xxx的一串 不含「p=」和末尾冒号)
   使用 dkim 方法验证自己的身份: privateDERKey 是 der 私钥, domain 是域名, selector 是记录名(xxx._domainkey中的xxx)
   注意： Gmail 对附件要求严格, 对于下列类型的附件(或包含这些文件类型的压缩文件), 即使使用 DKIM 认证仍然会被退信: 
     .ADE、.ADP、.BAT、.CHM、.CMD、.COM、.CPL、.EXE、.HTA、.INS、.ISP、
     .JAR、.JS、.JSE、.LIB、.LNK、.MDE、.MSC、.MSI、.MSP、.MST、.PIF、
     .SCR、.SCT、.SHB、.SYS、.VB、.VBE、.VBS、.VXD、.WSC、.WSF、.WSH
   // 发给 Gmail 的邮件中附件类型严格受限，参加: https://support.google.com/mail/answer/6590
   ```
2. I18N
    翻译工具类.
    <pre>
    [main]$ xgettext -k__ -k_x:2c,1 -k_n:1,2 -k_nx:3c,1,2 -o resources/Message.pot java/pack/age/Clazz.java --from-code UTF-8 
    [main]$ msgfmt --java2 -d resources -r Message -l zh_CN resources\Message_zh_CN.po 
    #(--source生成 java 文件) addResource("id", ResourceBundle.getBundle("Message"));
    也可使用 Poedit 工具抽取待翻译字符串【复数编辑nplurals=2; plural=n == 1 ? 0 : 1;】
    </pre>
    
    * <code>__("str");</code>
    * <code>_f("fmt",args...);</code>
    * <code>__("msg",domain,args...);</code>
    * <code>__("msg",resourceBundle,args...);</code>
    * <code>_x("str","context");</code>
    * <code>_n("single","plural",n);</code>
    * <code>_nx("single","plural",n,"context");</code>
    * <code>_n("One Apple", "{0} Apples", n, param)</code>
    * ...

