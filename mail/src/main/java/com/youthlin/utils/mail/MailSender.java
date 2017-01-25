package com.youthlin.utils.mail;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * SMTP 邮件发送工具.
 * <p>
 * Created by lin on 2017-01-23-023.
 * <p>
 * 示例:
 * <pre>
 * new MailSender()
 *      .start(new MailSender.SessionBuilder()
 *          .host("host")
 *          .auth("username", "password")
 *          .ssl(465)   // JDK 7 OK. JDK8 由于安全原因需要替换俩 jar 包
 *          .debug(true)
 *          .toSession()
 *      )//Session 只能调用一次 start
 *      .from("email", "DisplayName")//发件人 email,name
 *      .to("to", "DisplayName")//收件人
 *      .cc("cc")//抄送
 *      .cc("another", "name")
 *      .bcc("bcc")//密送
 *      .subject("subject")//主题
 *      .text("content")//只能设置一次内容. 或[html("html content")][content("plain",false)][content("html",true);]
 *      .attachment("path/to/file", "cid")//内嵌附件
 *      .attachment("path/to/file")//普通附件
 *      .attachment(file)//普通附件
 *      .send();//发送
 * </pre>
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue", "unused"})
public class MailSender {
    /**
     * Session 构造器.
     * Session 是与服务器通信的前提环境, 如 host username password 等在此设置.
     * <p>
     * 示例:
     * <pre>
     * new MailSender.SessionBuilder()
     *         .host("host")
     *         .auth("username", "password")
     *         .ssl(465)
     *         .toSession()
     * </pre>
     */
    public static class SessionBuilder {
        private final Properties props = new Properties();
        private Authenticator authenticator = null;

        public SessionBuilder host(String host) {
            props.put("mail.host", host);
            return this;
        }

        /**
         * 如果需要授权才能登录的服务器, 那么请提供账号和密码.
         */
        public SessionBuilder auth(final String username, final String password) {
            props.put("mail.smtp.auth", true);
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
            return this;
        }

        /**
         * 端口号，默认25
         */
        public SessionBuilder port(int port) {
            props.put("mail.smtp.port", port);
            return this;
        }

        /**
         * 需要 SSL 安全连接, 则请提供端口.
         * <p>
         * JDK8 不能使用 SSL, 需要替换 <code>JDK_HOME/jre/lib/security/</code> 下的两个 jar 包, 下载地址见下
         *
         * @see <a href="http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html">http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html</a>
         */
        public SessionBuilder ssl(int port) {
            props.put("mail.smtp.ssl.enable", true);//使用 SSL
            props.put("mail.smtp.socketFactory.port", port);
            return port(port);
        }

        /**
         * 是否开启 Debug 输出, 默认否.
         */
        public SessionBuilder debug(boolean debug) {
            props.put("mail.debug", Boolean.toString(debug));//必须要是 String 类型
            return this;
        }

        public Session toSession() {
            return Session.getInstance(props, authenticator);
        }
    }

    //region //field
    private static final String default_charset = "UTF-8";//默认字符编码
    private final MimeMultipart content = new MimeMultipart();//邮件的所有内容(body+Attachment)
    private final BodyPart body = new MimeBodyPart();//body
    private final List<BodyPart> attachments = new ArrayList<BodyPart>();//attachments
    private boolean started = false;//是否已经设置 Session
    private boolean contentHasSet = false;//是否已经设置过内容
    private Message msg;//每次设置的 Message 主体
    //endregion //field

    /**
     * 从 Session 构造 Message.
     *
     * @throws IllegalStateException 当已经调用过本方法再次调用时抛出
     */
    public MailSender start(Session session) {
        if (started) {
            throw new IllegalStateException("start() method already called.");
        }
        started = true;
        msg = new MimeMessage(session);
        return this;
    }

    public MailSender from(String email) throws MessagingException {
        msg.setFrom(new InternetAddress(email));
        return this;
    }

    public MailSender from(String email, String name) throws MessagingException {
        try {
            msg.setFrom(new InternetAddress(email, name, default_charset));
        } catch (UnsupportedEncodingException e) {
            from(email);
        }
        return this;
    }

    //region  //recipients
    public MailSender to(String email) throws MessagingException {
        return to(new String[]{email}, null);
    }

    public MailSender to(String email, String name) throws MessagingException {
        return to(new String[]{email}, new String[]{name});
    }

    public MailSender to(String[] emails) throws MessagingException {
        return to(emails, null);
    }

    public MailSender to(String[] emails, String[] names) throws MessagingException {
        return addRecipients(Message.RecipientType.TO, toAddresses(emails, names));
    }

    public MailSender cc(String email) throws MessagingException {
        return cc(new String[]{email}, null);
    }

    public MailSender cc(String email, String name) throws MessagingException {
        return cc(new String[]{email}, new String[]{name});
    }

    public MailSender cc(String[] emails) throws MessagingException {
        return cc(emails, null);
    }

    public MailSender cc(String[] emails, String[] names) throws MessagingException {
        return addRecipients(Message.RecipientType.CC, toAddresses(emails, names));
    }

    public MailSender bcc(String email) throws MessagingException {
        return bcc(new String[]{email}, null);
    }

    public MailSender bcc(String email, String name) throws MessagingException {
        return bcc(new String[]{email}, new String[]{name});
    }

    public MailSender bcc(String[] emails) throws MessagingException {
        return bcc(emails, null);
    }

    public MailSender bcc(String[] emails, String[] names) throws MessagingException {
        return addRecipients(Message.RecipientType.BCC, toAddresses(emails, names));

    }

    private Address[] toAddresses(String[] emails, String[] names) throws AddressException {
        int eLen = emails.length;
        Address[] addresses = new Address[eLen];
        if (names != null && names.length == eLen) {//email 和 name 能对上时
            try {
                for (int i = 0; i < eLen; i++) {
                    addresses[i] = new InternetAddress(emails[i], names[i], default_charset);
                }
                return addresses;
            } catch (UnsupportedEncodingException ignore) {
            }
        }
        for (int i = 0; i < eLen; i++) {//对不上 或 异常
            addresses[i] = new InternetAddress(emails[i]);
        }
        return addresses;
    }

    private MailSender addRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
        msg.addRecipients(type, addresses);
        return this;
    }
    //endregion  //recipients

    public MailSender subject(String subject) throws MessagingException {
        msg.setSubject(subject);
        return this;
    }

    //region //content
    public MailSender html(String html) throws MessagingException {
        return content(html, true);
    }

    public MailSender text(String plain) throws MessagingException {
        return content(plain, false);
    }

    /**
     * 设置邮件内容.
     * <p>
     * 也可以使用 <code>html()</code> 或 <code>text()</code> 设置, 但只能设置一次内容, 否则将抛出异常.
     *
     * @throws IllegalStateException 当多次设置邮件内容时抛出.
     */
    public MailSender content(String content, boolean isHtml) throws MessagingException {
        if (contentHasSet) {
            throw new IllegalStateException("Content already set.");
        }
        contentHasSet = true;
        if (isHtml) {
            body.setContent(content, "text/html;charset=" + default_charset);
        } else {
            body.setContent(content, "text/plain;charset=" + default_charset);
        }
        return this;
    }
    //endregion //content

    //region //attachment

    /**
     * 带附件.
     * <p>
     * 在「附件」中显示.
     */
    public MailSender attachment(String pathToFile) throws MessagingException {
        return attachment(pathToFile, null);
    }

    /**
     * 带附件.
     * <p>
     * 没有设置 cid 则只在附件中显示，设置了 cid 则 对应 html 邮件内容. 如：&lt;img src="cid:img1"/>
     * Outlook - cid 只支持图片, 文件的 cid 在 a 标签 href 属性中不起作用，只在附件中显示
     * QQ      - cid 支持二进制文件，有 cid 的附件将不会在「附件」中显示
     */
    public MailSender attachment(String pathToFile, String cid) throws MessagingException {
        return attachment(new File(pathToFile), cid);
    }

    public MailSender attachment(File file) throws MessagingException {
        return attachment(file, null);
    }

    public MailSender attachment(File file, String cid) throws MessagingException {
        BodyPart attach = new MimeBodyPart();
        attach.setDataHandler(new DataHandler(new FileDataSource(file)));
        try {
            attach.setFileName(MimeUtility.encodeWord(file.getName(), default_charset, null));
        } catch (UnsupportedEncodingException ignore) {
        }
        if (cid != null) {
            attach.setHeader("Content-ID", cid);
        }
        attachments.add(attach);
        return this;
    }
    //endregion //attachment

    public Message toMessage() throws MessagingException {
        content.removeBodyPart(body);//防止多次调用添加多次
        content.addBodyPart(body);//顺序：body 在 attachment 之前
        for (BodyPart attach : attachments) {
            content.removeBodyPart(attach);
            content.addBodyPart(attach);
        }
        msg.setContent(content);
        return msg;
    }

    public void send() throws MessagingException {
        Transport.send(toMessage(), msg.getAllRecipients());
    }
}
