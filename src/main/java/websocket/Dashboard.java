package websocket;
import jakarta.ejb.Singleton;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.*;

import java.io.IOException;
import java.util.HashMap;


@Singleton
@ServerEndpoint("/websocket/dashboard/{token}")
public class Dashboard {
    HashMap<String, Session> sessions = new HashMap<String, Session>();

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
        for (String key : sessions.keySet()) {
            if (sessions.get(key) == session)
                sessions.remove(key);
        }
    }
    @OnMessage
    public void onMessage(Session session, String msg) {
        System.out.println("Message received: " + msg);
    }

}