package th.yzw.specialrecorder.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class MergeFileEmailHelper {
    private static final String TAG = "殷宗旺";
    //    private final String username = EncryptAndDecrypt.decryptPassword("QSZktAEIl8xySso4XzpNG+TLcvX4suXD");
    private final String username = "specialrecorder@163.com";
    private final String password = "DZKVHRBZQVGJVEAN";

    public Session getReceiveSessionBySmtp() {
        String protocol = "smtp";
        String host = "smtp.163.com";
        String port = "25";
        // 准备连接服务器的会话信息
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", protocol); // 协议
        props.setProperty("mail.smtp.port", port); // 端口
        props.setProperty("mail.smtp.host", host); // smtp服务器
        // 创建Session实例对象
        return Session.getInstance(props);
    }

    public Store getInbox() throws MessagingException {
        Store store = getReceiveSessionBySmtp().getStore("smtp");
        store.connect(username, password);
        return store;
    }

    public String getSubject(Message msg) throws UnsupportedEncodingException, MessagingException {
        return MimeUtility.decodeText(msg.getSubject());
    }

    public String getFrom(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        String from = "";
        Address[] froms = msg.getFrom();
        if (froms.length < 1) {
            throw new MessagingException("没有发件人信息！");
        }
        InternetAddress address = (InternetAddress) froms[0];
        String person = address.getPersonal();
        if (person != null)
            person = MimeUtility.decodeText(person) + " ";
        else
            person = "";
        from = person + "<" + address.getAddress() + ">";
        return from;
    }

    public String getReceiveAddress(MimeMessage msg, Message.RecipientType type) throws MessagingException {
        StringBuilder receiveAddress = new StringBuilder();
        Address[] addresss = null;
        if (type == null) {
            addresss = msg.getAllRecipients();
        } else {
            addresss = msg.getRecipients(type);
        }
        if (addresss == null || addresss.length < 1)
            throw new MessagingException("没有收件人!");
        for (Address address : addresss) {
            InternetAddress internetAddress = (InternetAddress) address;
            receiveAddress.append(internetAddress.toUnicodeString()).append(",");
        }
        receiveAddress.deleteCharAt(receiveAddress.length() - 1); //删除最后一个逗号
        return receiveAddress.toString();
    }

    public String getSentDate(MimeMessage msg, String pattern) throws MessagingException {
        Date receivedDate = msg.getSentDate();
        if (receivedDate == null)
            return "";
        if (pattern == null || "".equals(pattern))
            pattern = "yyyy年MM月dd日 E HH:mm ";

        return new SimpleDateFormat(pattern, Locale.CHINA).format(receivedDate);
    }

    public boolean isContainAttachment(Part part) throws MessagingException, IOException {
        boolean flag = false;
        if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    flag = true;
                } else if (bodyPart.isMimeType("multipart/*")) {
                    flag = isContainAttachment(bodyPart);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.contains("application")) {
                        flag = true;
                    }
                    if (contentType.contains("name")) {
                        flag = true;
                    }
                }
                if (flag) break;
            }
        } else if (part.isMimeType("message/rfc822")) {
            flag = isContainAttachment((Part) part.getContent());
        }
        return flag;
    }

    public boolean isSeen(MimeMessage msg) throws MessagingException {
        return msg.getFlags().contains(Flags.Flag.SEEN);
    }

    public boolean isReplySign(MimeMessage msg) throws MessagingException {
        boolean replySign = false;
        String[] headers = msg.getHeader("Disposition-Notification-To");
        if (headers != null)
            replySign = true;
        return replySign;
    }

    public String getPriority(MimeMessage msg) throws MessagingException {
        String priority = "普通";
        String[] headers = msg.getHeader("X-Priority");
        if (headers != null) {
            String headerPriority = headers[0];
            if (headerPriority.contains("1") || headerPriority.contains("High"))
                priority = "紧急";
            else if (headerPriority.contains("5") || headerPriority.contains("Low"))
                priority = "低";
            else
                priority = "普通";
        }
        return priority;
    }

    public void getMailTextContent(Part part, StringBuilder content) throws MessagingException, IOException {
        //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;

//        else if (part.isMimeType("text/html") && !isContainTextAttach) {
//            String s = part.getContent().toString().trim();
//            String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
//            Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
//            Matcher m_html = p_html.matcher(s);
//            s = m_html.replaceAll(""); // 过滤html标签
//            content.append(s);
//        }
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
            Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            String s = part.getContent().toString().trim();
            Matcher m_html = p_html.matcher(s);
            s = m_html.replaceAll(""); // 过滤html标签
//            s = s.replaceAll("&#xff1a;","：").replaceAll("&#xff0c;","，");
            content.append(s);
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content);
            }
        }
    }

    public void saveAttachment(Part part, String destDir) throws UnsupportedEncodingException, MessagingException,
            FileNotFoundException, IOException {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();    //复杂体邮件
            //复杂体邮件包含多个邮件体
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                //获得复杂体邮件中其中一个邮件体
                BodyPart bodyPart = multipart.getBodyPart(i);
                //某一个邮件体也有可能是由多个邮件体组成的复杂体
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    FileTools.saveFile(bodyPart.getInputStream(), destDir, decodeText(bodyPart.getFileName()));
                } else if (bodyPart.isMimeType("multipart/*")) {
                    saveAttachment(bodyPart, destDir);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.contains("name") || contentType.contains("application")) {
                        FileTools.saveFile(bodyPart.getInputStream(), destDir, decodeText(bodyPart.getFileName()));
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachment((Part) part.getContent(), destDir);
        }
    }

    public void saveAttachment(Part part, String destDir, List<File> fileList) throws UnsupportedEncodingException, MessagingException,
            FileNotFoundException, IOException {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();    //复杂体邮件
            //复杂体邮件包含多个邮件体
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                //获得复杂体邮件中其中一个邮件体
                BodyPart bodyPart = multipart.getBodyPart(i);
                //某一个邮件体也有可能是由多个邮件体组成的复杂体
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    FileTools.saveFile(bodyPart.getInputStream(), destDir, decodeText(bodyPart.getFileName()), fileList);
                } else if (bodyPart.isMimeType("multipart/*")) {
                    saveAttachment(bodyPart, destDir, fileList);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.contains("name") || contentType.contains("application")) {
                        FileTools.saveFile(bodyPart.getInputStream(), destDir, decodeText(bodyPart.getFileName()), fileList);
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachment((Part) part.getContent(), destDir, fileList);
        }
    }

    public String decodeText(String encodeText) throws UnsupportedEncodingException {
        if (encodeText == null || "".equals(encodeText)) {
            return "";
        } else {
            return MimeUtility.decodeText(encodeText);
        }
    }

    public class MyAuthenticator extends Authenticator {
        MyAuthenticator() {
            super();
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
}
