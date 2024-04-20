package websocket;
import dto.Task;
import dto.User;
import dto.UserDto;
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


@ServerEndpoint("/user/{token}")
public class UserEndpoint {


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
        User user = userBean.findUserByUsername(message);

        if (user == null) {
            UserDto userDto = new UserDto();
            userDto.setUsername(message);
            session.getAsyncRemote().sendObject(gson.toJson(user));
        } else {
            UserDto userDto = new UserDto();
            userDto.setUsername(user.getUsername());
            userDto.setName(user.getName());
            userDto.setEmail(user.getEmail());
            userDto.setContactNumber(user.getContactNumber());
            userDto.setRole(user.getRole());
            userDto.setUserPhoto(user.getUserPhoto());
            session.getAsyncRemote().sendObject(gson.toJson(userDto));
        }
    }

        public void sendUser(String username) {
            for (Session session : sessions.values()) {
                session.getAsyncRemote().sendObject(username);
            }
        }
    }
