package websocket;
import jakarta.ejb.Singleton;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@Singleton
@ServerEndpoint("/websocket/dashboard/{token}")
public class Dashboard {
    private Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<>());

    public void send(String msg) {
        sessions.values().forEach(session -> {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                System.out.println("Error in sending message to session " + session.getId() + ": " + e.getMessage());
            }
        });
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        System.out.println("A new WebSocket session is opened for client with token: " + token);
        sessions.put(token, session);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Websocket session is closed with CloseCode: " + reason.getCloseCode() + ": " + reason.getReasonPhrase());

        // Sincronizar acesso ao HashMap sessions
        synchronized (sessions) {
            Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Session> entry = iterator.next();
                if (entry.getValue() == session) {
                    iterator.remove();
                    break;
                }
            }
        }
    }
    @OnMessage
    public void onMessage(Session session, String msg) {
        System.out.println("Message received: " + msg);
    }

}