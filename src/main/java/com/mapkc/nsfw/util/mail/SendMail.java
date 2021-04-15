//package com.mapkc.nsfw.util.mail;
//
//
//import org.apache.commons.mail.HtmlEmail;
//
///**
// * Created by chy on 14-11-26.
// */
//public class SendMail {
//
//
//    public static final void send(String toAddress, String subject, String textmsg, String htmlMsg, String smtpHostName, String fromAddress, String fromName,
//                                  String smtpUsername, String smtpPassword
//    ) {
//
//       // javax.mail.internet.InternetAddress.InternetAddress address=	new InternetAddress(toAddress);
//
//
//
//        String msgID = null;
//        try {
//
//
//            HtmlEmail email = new HtmlEmail();
//            email.setCharset("UTF-8");
//            email.setHostName(smtpHostName);
//            email.setFrom(fromAddress, fromName, "UTF-8");
//            if (smtpUsername != null && smtpPassword != null)
//                email.setAuthentication(smtpUsername, smtpPassword);
//            email.addTo(toAddress);
//            email.setSubject(subject);
//            if (textmsg != null)
//                email.setTextMsg(textmsg);
//            if (htmlMsg != null)
//                email.setHtmlMsg(htmlMsg);
//            //email.setContent(msg, "text/html;charset = UTF-8"); // 这里就支持HTML代码的
//            msgID = email.send();
//            //log.debug("Send email ok to:{},subject:{},msgId:{}", toAddress, subject, msgID);
//        } catch (Exception e) {
//            //log.error("Error while send email to:{},subject:{},txtmsg:{},html:{}", e, toAddress, subject, textmsg, htmlMsg);
//
//        }
//
//    }
//}
