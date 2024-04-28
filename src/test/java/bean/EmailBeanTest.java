package bean;

import dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmailBeanTest {

    @Mock
    private UserBean userBean;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailBean emailBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testSendEmailWithInvalidRecipient() {
        String to = "invalid email";
        String subject = "Test Subject";
        String body = "Test Body";

        boolean result = emailBean.sendEmail(to, subject, body);

        assertFalse(result);
    }

    @Test
    void testSendConfirmationEmailWithInvalidUser() {
        User user = new User();
        user.setEmail("invalid email");
        user.setName("Test User");
        String confirmationToken = "testToken";
        LocalDateTime creationDate = LocalDateTime.now();

        boolean result = emailBean.sendConfirmationEmail(user, confirmationToken, creationDate);

        assertFalse(result);
    }

    @Test
    void testSendPasswordRecoverEmailWithInvalidUser() {
        User user = new User();
        user.setEmail("invalid email");
        user.setName("Test User");
        String confirmationToken = "testToken";
        LocalDateTime creationDate = LocalDateTime.now();

        boolean result = emailBean.sendPasswordRecoverEmail(user, confirmationToken, creationDate);

        assertFalse(result);
    }
}