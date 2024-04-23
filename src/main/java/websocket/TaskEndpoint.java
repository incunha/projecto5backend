package websocket;
import dto.Task;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnClose;
import jakarta.websocket.Session;
import jakarta.websocket.OnMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import bean.TaskBean;
import bean.UserBean;
import com.google.gson.Gson;
import dto.TaskWebsocketDto;

@ApplicationScoped
@ServerEndpoint("/task/{token}")
public class TaskEndpoint {

    @Inject
    private TaskBean taskBean;
    @Inject
    private UserBean userBean;


    private static final Logger LOGGER = Logger.getLogger(TaskEndpoint.class.getName());
    private static Map<String, Session> sessions = new ConcurrentHashMap<>();
    public static Map<String, Session> getSessions() {
        return sessions;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        sessions.put(token, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("token") String token) {
        sessions.remove(token);
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        System.out.println("Message received: " + msg);
    }


    public void send(TaskWebsocketDto taskWebsocketDto) {
        sessions.values().forEach(session -> {
            try {
                Gson gson = new Gson();
                String msg = gson.toJson(taskWebsocketDto);

                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                System.out.println("Error in sending message to session " + session.getId() + ": " + e.getMessage());
            }
        });
    }

}
