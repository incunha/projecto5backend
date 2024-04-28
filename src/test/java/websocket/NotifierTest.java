package websocket;

import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpointConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class NotifierTest {

    @Mock
    private Session session;

    private Notifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        notifier = new Notifier();
    }

    @Test
    void testOnOpen() {
        String username = "testUser";

        notifier.onOpen(session, username);

        assert(Notifier.getSessions().containsKey(username));
    }

    @Test
    void testOnClose() {
        String username = "testUser";

        notifier.onOpen(session, username);
        notifier.onClose(session, username);

        assert(!Notifier.getSessions().containsKey(username));
    }


}