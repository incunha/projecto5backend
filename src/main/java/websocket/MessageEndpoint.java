package websocket;

import bean.MessageBean;
import bean.UserBean;
import bean.NotificationBean;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.ArrayList;

@ServerEndpoint("/chat/{token}/{username}")
public class MessageEndpoint {
    @Inject
    private MessageDao messageDao;
    @Inject
    private UserDao userDao;
    @Inject
    private UserBean userBean;
    @Inject
    private MessageBean messageBean;
    @Inject
    private NotificationBean notificationBean; // Injetar o NotificationBean

    private static final Logger LOGGER = Logger.getLogger(MessageEndpoint.class.getName());
    // Existing private variable
    private static Map<String, Session> sessions = new ConcurrentHashMap<>();

    // Add this getter method
    public static Map<String, Session> getSessions() {
        return sessions;
    }

    public void send(String message, String token, String username) {
        String conversationId = token + username;
        Session session = sessions.get(conversationId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token, @PathParam("username") String username) {
        String conversationId = token + username;
        System.out.println("Conversation ID: " + conversationId);
        sessions.put(conversationId, session);

        LOGGER.info("Session opened and added to sessions map." + conversationId);

    }

    @OnClose
    public void onClose(Session session, @PathParam("token") String token, @PathParam("username") String username) {
        String conversationId = token + username;
        sessions.remove(conversationId);
        LOGGER.info("Session closed and removed from sessions map." + conversationId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Processing message from " + session.getId());

        Gson gson = new Gson();
        MessageDto messageDto = gson.fromJson(message, MessageDto.class);

        // Buscar UserEntity para o remetente e o destinatário
        UserEntity sender = userDao.findUserByUsername(messageDto.getSender());
        System.out.println("Sender: " + sender.getUsername());
        UserEntity receiver = userDao.findUserByUsername(messageDto.getReceiver());
        System.out.println("Receiver: " + receiver.getUsername());

        // Verificar se o remetente e o destinatário existem
        if (sender != null && receiver != null) {
            messageBean.sendMessage(sender, receiver, messageDto.getMessage());

            // Enviar a mensagem para o remetente independentemente do estado de login do destinatário
            send(gson.toJson(messageDto), sender.getToken(), messageDto.getReceiver());

            if (messageBean.isReceiverloggedIn(receiver, sender) != null) {
                messageBean.markMessagesAsRead(sender, receiver);
                send(gson.toJson(messageDto), receiver.getToken(), messageDto.getSender());
            } else {
                // Enviar uma notificação para o destinatário
                Notifier.sendNotification(messageDto.getReceiver(), "New message from " + messageDto.getSender());

                // Persistir a notificação no banco de dados
                notificationBean.sendNotification(sender, receiver, "New message from " + messageDto.getSender());
            }
        }
    }
}