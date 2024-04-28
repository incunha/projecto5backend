package websocket;

import dto.TaskWebsocketDto;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import service.ObjectMapperContextResolver;

import static org.mockito.Mockito.*;

public class TaskEndpointTest {

    @Mock
    private Session session;

    @Mock
    private RemoteEndpoint.Basic basicRemote;

    @Mock
    private ObjectMapperContextResolver objectMapperContextResolver;

    @InjectMocks
    private TaskEndpoint taskEndpoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(session.getBasicRemote()).thenReturn(basicRemote);
    }

    @Test
    void testOnOpen() {
        String token = "testToken";
        taskEndpoint.onOpen(session, token);
        // Verify that the session was added to the sessions map
    }

    @Test
    void testOnClose() {
        String token = "testToken";
        taskEndpoint.onClose(session, token);
        // Verify that the session was removed from the sessions map
    }

    @Test
    void testOnMessage() {
        String message = "testMessage";
        taskEndpoint.onMessage(session, message);
        // Verify that the message was processed correctly
    }

}