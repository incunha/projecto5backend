package websocket;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpointConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class DashboardTest {

    private Dashboard dashboard;

    @Mock
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        dashboard = new Dashboard();
    }

    @Test
    void testOnOpen() {
        String token = "token";

        dashboard.onOpen(session, token);

        verify(session, times(0)).getAsyncRemote();
    }

    @Test
    void testOnClose() {
        CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test");

        dashboard.onClose(session, closeReason);

        verify(session, times(0)).getAsyncRemote();
    }

    @Test
    void testOnMessage() {
        String message = "Test message";

        dashboard.onMessage(session, message);

        verify(session, times(0)).getAsyncRemote();
    }


}