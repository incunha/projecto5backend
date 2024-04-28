package bean;
import dto.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

@Stateless
public class EmailBean {

    @EJB
    private UserBean userBean;
    private final String username = "ines_bcunha@hotmail.com";
    private final String password = System.getenv("SMTP_PASSWORD");
    private final String host = "smtp-mail.outlook.com";
    private final int port = 587;

    private static final Logger LOGGER = LogManager.getLogger(EmailBean.class);
    public EmailBean() {
    }

    public boolean sendEmail(String to, String subject, String body) {
        LOGGER.info("sendEmail method called");
        boolean sent = false;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        LOGGER.info("Sending email to " + to + "...");

        try {
            System.out.println("Sending email to " + to + "...");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            System.out.println("Sending email...");
            Transport.send(message);
            sent = true;
            LOGGER.info("Email sent successfully.");
        } catch (MessagingException e) {
            sent = false;
            e.printStackTrace();
            LOGGER.error("Error while sending email: ", e);
        }

        LOGGER.info("Email sent: " + sent);
        return sent;
    }

    public boolean sendConfirmationEmail(User user, String confirmationToken, LocalDateTime creationDate) {
        LOGGER.info("sendConfirmationEmail method called");
        boolean sent = false;

        String userEmail = user.getEmail();
        String subject = "Scrum - Account Confirmation";
        String confirmationLink = "http://localhost:5173/confirm-account?token=" + confirmationToken;
        String body = "Dear " + user.getName() + ",\n\n"
                + "Thank you for registering with us. Please click on the link below to confirm your account.\n\n"
                + "Confirmation Link: " + confirmationLink;

        if (sendEmail(userEmail, subject, body)) {
            LOGGER.info("Email sent successfully.");
            sent = true;
        } else {
            // Verifica se já se passaram mais de 48 horas desde a criação do user
            LocalDateTime now = LocalDateTime.now();
            long hoursSinceCreation = ChronoUnit.HOURS.between(creationDate, now);
            if (hoursSinceCreation > 48) {
                userBean.removeUser(user.getUsername());
            }
        }
        LOGGER.info("Email sent: " + sent);
        return sent;
    }

    public boolean sendPasswordRecoverEmail(User user, String confirmationToken, LocalDateTime creationDate) {
        LOGGER.info("sendPasswordRecoverEmail method called");
        boolean sent = false;

        String userEmail = user.getEmail();
        String subject = "Scrum - Password Recovery";
        String recuperationLink = "http://localhost:5173/recover-password?token=" + confirmationToken;
        String body = "Dear " + user.getName() + ",\n\n"
                + "You have requested to recover your password. Please click on the link below to reset your password.\n\n"
                + "Password Recovery Link: " + recuperationLink;

        if (sendEmail(userEmail, subject, body)) {
            LOGGER.info("Email sent successfully.");
            sent = true;
        }
        LOGGER.info("Email sent: " + sent);
        return sent;
    }
}