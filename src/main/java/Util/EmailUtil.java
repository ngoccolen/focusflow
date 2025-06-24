package Util;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;


public class EmailUtil {
    private static final String FROM_EMAIL = "lenguyenngoc2006qb@gmail.com";
    private static final String PASSWORD = "xkqfgcftljxwbcha";

    public static boolean sendEmail(String toEmail, String subject, String content) {

        System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            String htmlContent = "<html><body>"
                + "<h3>Password Reset Request</h3>"
                + "<p>Your OTP code is: <strong>" + content + "</strong></p>"
                + "<p>This code will expire in 5 minutes.</p>"
                + "</body></html>";

            message.setContent(htmlContent, "text/html; charset=UTF-8");

            System.out.println("Sending email to: " + toEmail);
            Transport.send(message);
            System.out.println("Email sent successfully.");
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
