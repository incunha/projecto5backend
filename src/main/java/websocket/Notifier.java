package websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.PathParam;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


@ApplicationScoped
@ServerEndpoint("/notifications/{username}")
public class Notifier {
    private static final Logger LOGGER = Logger.getLogger(Notifier.class.getName());
    private static Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        sessions.put(username, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(username);
    }

    public static void sendNotification(String username, String notification) {
        Session userSession = sessions.get(username);

        if (userSession != null && userSession.isOpen()) {
            userSession.getAsyncRemote().sendText(notification);
        }
    }

    public static void sendLogoutNotification(String username) {
        Session userSession = sessions.get(username);

        if (userSession != null && userSession.isOpen()) {
            userSession.getAsyncRemote().sendText("LOGOUT");
            System.out.println("LOGOUT");
        }
    }

    public static Map<String, Session> getSessions() {
        return sessions;
    }
}
