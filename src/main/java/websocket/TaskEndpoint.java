package websocket;
import dto.Task;
import jakarta.inject.Inject;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnClose;
import jakarta.websocket.Session;
import jakarta.websocket.OnMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import bean.TaskBean;
import bean.UserBean;
import com.google.gson.Gson;
import dto.TaskWebsocketDto;


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
    public void onMessage(String message, Session session) {
        System.out.println("Message received: " + message);
        LOGGER.info("Processing message from " + session.getId());
        Gson gson = new Gson();
        Task task = taskBean.findTaskById(gson.fromJson(message, String.class));

        if(task == null) {
            TaskWebsocketDto taskWebsocketDto = new TaskWebsocketDto();
            taskWebsocketDto.setTaskId(message);
            session.getAsyncRemote().sendObject(gson.toJson(task));
            for (Session s : sessions.values()) {
                s.getAsyncRemote().sendObject(gson.toJson(task));
            }
        } else {
            TaskWebsocketDto taskWebsocketDto = new TaskWebsocketDto();
            taskWebsocketDto.setTask(task);
            taskWebsocketDto.setTaskId(message);
            session.getAsyncRemote().sendObject(gson.toJson(task));
            for (Session s : sessions.values()) {
                s.getAsyncRemote().sendObject(gson.toJson(task));
            }
        }
    }


    public void sendTask(String taskId) {
        for (Session session : sessions.values()) {
            session.getAsyncRemote().sendObject(taskId);
        }
    }
}
