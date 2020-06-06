package th.yzw.specialrecorder.tools;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public final class SendEmailHelper {
    private final String username = EncryptAndDecrypt.decryptPassword("QSZktAEIl8xySso4XzpNG+TLcvX4suXD");
    private final String password = "DZKVHRBZQVGJVEAN";
    private final String fromAdress = EncryptAndDecrypt.decryptPassword("QSZktAEIl8xySso4XzpNG+TLcvX4suXD");
    private final String toAdress = EncryptAndDecrypt.decryptPassword("XfVPYKaepUXyJL0k75Xim05AZesPPOf7");

    /**
     * 创建发送邮件会话
     *
     * @return 发送邮件会话实例
     */
    public Session getSendSession() {
        String protocol = "smtp";
        String host = "smtp.163.com";
        String port = "25";
        String is_auth = "true";
        String is_debug_mode = "false";

        // 准备连接服务器的会话信息
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", protocol);
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.auth", is_auth);
        props.setProperty("mail.debug", is_debug_mode);
        // 创建Session实例对象
        return Session.getInstance(props);
    }

    /**
     * 发送纯文字邮件
     *
     * @param content 邮件内容
     */
    public void sendTextEmail(String subject, String content) throws MessagingException {
        Session session = getSendSession();
        // 创建MimeMessage实例对象  
        MimeMessage message = new MimeMessage(session);
        // 设置发件人  
        message.setFrom(new InternetAddress(fromAdress));
        //设置邮件主题  
        message.setSubject(subject);
        // 设置收件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toAdress));
        // 设置发送时间
        message.setSentDate(new Date());
        // 设置纯文本内容为邮件正文
        message.setText(content);
        // 保存并生成最终的邮件内容 
        message.saveChanges();
        // 获得Transport实例对象 
        Transport transport = session.getTransport();
        // 打开连接 
        transport.connect(username, password);
        // 将message对象传递给transport对象，将邮件发送出去 
        transport.sendMessage(message, message.getAllRecipients());
        // 关闭连接 
        transport.close();
    }

    /**
     * 发送带附件邮件
     *
     * @param subject    主题
     * @param content    文字内容
     * @param attachList 附件列表
     */
    public void sendMultiEmail(String subject, String content, boolean hasCC, File... attachList) throws IOException, MessagingException {
        Session session = getSendSession();
        // 创建MimeMessage实例对象
        MimeMessage message = new MimeMessage(session);
        // 设置主题 
        message.setSubject(subject);
        // 设置发送人
        message.setFrom(new InternetAddress(fromAdress, "specialrecorder", "utf-8"));
        // 设置收件人  
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toAdress, "yinzongwang", "utf-8"));
        // 设置抄送 
        if (hasCC)
            message.setRecipient(Message.RecipientType.CC, new InternetAddress(fromAdress, "specialrecorder", "utf-8"));
        // message.setRecipient(RecipientType.CC, new InternetAddress("xyang0917@gmail.com","王五_gmail",charset));  
        // 设置密送 
        // message.setRecipient(RecipientType.BCC, new InternetAddress("xyang0917@qq.com", "赵六_QQ", charset));  
        // 设置回复人(收件人回复此邮件时,默认收件人) 
        // message.setReplyTo(InternetAddress.parse("\"" + MimeUtility.encodeText("田七") + "\" <417067629@qq.com>")); 
        //  设置优先级(1:紧急   3:普通    5:低) 
        // message.setHeader("X-Priority", "1"); 
        //  要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读) 
        // message.setHeader("Disposition-Notification-To", from); 
        //创建Multipart增加其他的parts
        Multipart mp = new MimeMultipart();
        //创建一个消息体
        MimeBodyPart msgBodyPart = new MimeBodyPart();
        // 设置纯文本内容为邮件正文
        msgBodyPart.setText(content);

        mp.addBodyPart(msgBodyPart);
        for (File file : attachList) {
            //创建文件附件
            MimeBodyPart fileBodyPart = new MimeBodyPart();
            fileBodyPart.attachFile(file);
            mp.addBodyPart(fileBodyPart);
        }
//        for (String path : paths) {
//            MimeBodyPart fileBodyPart = new MimeBodyPart();
//            fileBodyPart.attachFile(path);
//            mp.addBodyPart(fileBodyPart);
//        }

        //增加Multipart到消息体中
        message.setContent(mp);
        //设置日期
        message.setSentDate(new Date());
        //设置附件格式
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
        // 获得Transport实例对象 
        Transport transport = session.getTransport();
        // 打开连接 
        transport.connect(username, password);
        // 将message对象传递给transport对象，将邮件发送出去 
        transport.sendMessage(message, message.getAllRecipients());
        // 关闭连接 
        transport.close();
    }
}
