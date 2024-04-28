package websocket;

import jakarta.websocket.Session;
import dto.MessageDto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;

import websocket.MessageEndpoint;

public class MessageEndpointTest {

    private MessageEndpoint messageEndpoint;

    @Mock
    private Session session;

    private Map<String, Session> sessions;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        messageEndpoint = new MessageEndpoint();
        sessions = new ConcurrentHashMap<String, Session>();
    }

    @Test
    void testOnOpen() {
        String token = "token";
        String username = "username";
        String conversationId = token + username;

        messageEndpoint.onOpen(session, token, username);

        assertTrue(MessageEndpoint.getSessions().containsKey(conversationId));
    }

    @Test
    void testOnClose() {
        String token = "token";
        String username = "username";
        String conversationId = token + username;
        MessageEndpoint.getSessions().put(conversationId, session);

        messageEndpoint.onClose(session, token, username);

        assertFalse(MessageEndpoint.getSessions().containsKey(conversationId));
    }

    @Test
    void testOnOpen_SessionAlreadyExists() {
        String token = "token";
        String username = "username";
        String conversationId = token + username;
        MessageEndpoint.getSessions().put(conversationId, session);

        messageEndpoint.onOpen(session, token, username);

        assertTrue(MessageEndpoint.getSessions().containsKey(conversationId));
        assertEquals(session, MessageEndpoint.getSessions().get(conversationId));
    }

    @Test
    void testOnClose_SessionDoesNotExist() {
        String token = "token";
        String username = "username";
        String conversationId = token + username;

        messageEndpoint.onClose(session, token, username);

        assertFalse(MessageEndpoint.getSessions().containsKey(conversationId));
    }

    @Test
    void testOnMessage_SessionDoesNotExist() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String sender = "sender";
        String receiver = "receiver";
        String messageContent = "Hello!";
        LocalDateTime timestamp = LocalDateTime.now();

        MessageDto messageDto = new MessageDto();
        messageDto.setSender(sender);
        messageDto.setReceiver(receiver);
        messageDto.setMessage(messageContent);
        messageDto.setSendDate(timestamp);

        when(session.getId()).thenReturn("sessionId");
    }


}