package websocket;

import bean.MessageBean;
import bean.UserBean;
import dao.MessageDao;
import dao.UserDao;
import dto.MessageDto;
import entities.MessageEntity;
import entities.UserEntity;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import com.google.gson.Gson;
import jakarta.websocket.server.PathParam;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.ArrayList;

@ServerEndpoint("/chat/{username}")
public class MessageEndpoint {
    @Inject
    private MessageDao messageDao;
    @Inject
    private UserDao userDao;
    @Inject
    private UserBean userBean;
    @Inject
    private MessageBean messageBean;

    private static final Logger LOGGER = Logger.getLogger(MessageEndpoint.class.getName());
    private static Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        sessions.put(username, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(username);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Processing message from " + session.getId());

        Gson gson = new Gson();
        MessageDto messageDto = gson.fromJson(message, MessageDto.class);

        // Buscar UserEntity para o remetente e o destinatário
        UserEntity sender = userDao.findUserByUsername(messageDto.getSender());
        UserEntity receiver = userDao.findUserByUsername(messageDto.getReceiver());

        // Verificar se o remetente e o destinatário existem
        if (sender != null && receiver != null) {
            messageBean.sendMessage(sender, receiver, messageDto.getMessage());

            // Enviar uma notificação para o destinatário
            Notifier.sendNotification(messageDto.getReceiver(), "New message from " + messageDto.getSender());
        } else {
            LOGGER.warning("Sender or receiver not found");
        }
        Session senderSession = sessions.get(messageDto.getSender());
        Session receiverSession = sessions.get(messageDto.getReceiver());

        if (senderSession != null && senderSession.isOpen()) {
            senderSession.getAsyncRemote().sendText(message);
        }
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.getAsyncRemote().sendText(message);
        }
    }
}