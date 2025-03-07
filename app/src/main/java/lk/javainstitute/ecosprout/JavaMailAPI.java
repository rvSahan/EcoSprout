package lk.javainstitute.ecosprout;

import android.os.AsyncTask;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaMailAPI extends AsyncTask<Void, Void, String> {
    private final String senderEmail = "javavideo001@gmail.com"; // Use your email
    private final String senderPassword =  "yvsq xtap gxtu tkdq"; // Use generated App Password
    private final String recipientEmail;
    private final String subject;
    private final String messageBody;

    public JavaMailAPI(String recipientEmail, String subject, String messageBody) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.messageBody = messageBody;
    }

    @Override
    protected String doInBackground(Void... voids) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(messageBody);

            Transport.send(message);
            return "Email Sent Successfully!";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Email Sending Failed: " + e.getMessage();
        }
    }
}


