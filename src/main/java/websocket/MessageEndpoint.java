package websocket;

import bean.MessageBean;
import bean.UserBean;
import bean.NotificationBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.MessageDao;
import dao.UserDao;
import dto.MessageDto;
import entities.MessageEntity;
import entities.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import com.google.gson.Gson;
import jakarta.websocket.server.PathParam;
import service.ObjectMapperContextResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.ArrayList;

@ApplicationScoped
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
    private NotificationBean notificationBean;

    private static final Logger LOGGER = Logger.getLogger(MessageEndpoint.class.getName());
    // Existing private variable
    private static Map<String, Session> sessions = new ConcurrentHashMap<>();

    // Add this getter method
    public static Map<String, Session> getSessions() {
        return sessions;
    }

    ObjectMapperContextResolver contextResolver = new ObjectMapperContextResolver();

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

        ObjectMapper mapper = contextResolver.getContext(Object.class);
        MessageDto messageDto;
        try {
            messageDto = mapper.readValue(message, MessageDto.class);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao desserializar a mensagem JSON", e);
        }

        // Buscar UserEntity para o remetente e o destinatário
        UserEntity sender = userDao.findUserByUsername(messageDto.getSender());
        sender.setLastInteraction(LocalDateTime.now());
        userDao.updateUser(sender);
        System.out.println("Sender: " + sender.getUsername());
        UserEntity receiver = userDao.findUserByUsername(messageDto.getReceiver());
        System.out.println("Receiver: " + receiver.getUsername());

        // Verificar se o remetente e o destinatário existem
        if (sender != null && receiver != null) {
            messageBean.sendMessage(sender, receiver, messageDto.getMessage());
            sender.setLastInteraction(LocalDateTime.now());

            // Verificar se o destinatário está na sessão
            String receiverSessionId = receiver.getToken() + messageDto.getSender();
            if (sessions.containsKey(receiverSessionId)) {
                // Se o destinatário estiver na sessão, marcar a mensagem como lida
                messageBean.markMessagesAsRead(sender, receiver);
                messageDto.setRead(true);
                try {
                    send(mapper.writeValueAsString(messageDto), receiver.getToken(), messageDto.getSender());
                    send(mapper.writeValueAsString(messageDto), sender.getToken(), messageDto.getReceiver());
                } catch (IOException e) {
                    throw new RuntimeException("Falha ao serializar a mensagem para JSON", e);
                }
            } else {
                // Se o destinatário não estiver na sessão, enviar uma notificação
                Notifier.sendNotification(messageDto.getReceiver(), "New message from " + messageDto.getSender());
                try {
                    send(mapper.writeValueAsString(messageDto), sender.getToken(), messageDto.getReceiver());
                } catch (IOException e) {
                    throw new RuntimeException("Falha ao serializar a mensagem para JSON", e);
                }
                // Persistir a notificação no banco de dados
                notificationBean.sendNotification(sender, receiver, "New message from " + messageDto.getSender());
            }
        }
    }
}