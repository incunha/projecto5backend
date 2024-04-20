package bean;

import jakarta.websocket.Session;
import dao.MessageDao;
import dto.MessageDto;
import entities.MessageEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static websocket.MessageEndpoint.getSessions;

@Stateless
public class MessageBean {

    @EJB
    private MessageDao messageDao;

    private static final Logger LOGGER = Logger.getLogger(MessageBean.class.getName());

    public void sendMessage(UserEntity sender, UserEntity receiver, String messageContent) {
        LOGGER.info("sendMessage method called");
        try {
            MessageEntity messageEntity = new MessageEntity();
            messageEntity.setSender(sender);
            messageEntity.setReceiver(receiver);
            messageEntity.setMessage(messageContent);
            messageEntity.setTimestamp(LocalDateTime.now());
            messageEntity.setRead(false);

            messageDao.createMessage(messageEntity);
            LOGGER.info("Message sent and saved in the database.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while sending and saving the message: ", e);
        }
    }

    public List<MessageDto> getMessagesBetweenUsers(UserEntity user1, UserEntity user2) {
        List<MessageEntity> messageEntities = messageDao.findMessageByUser(user1, user2);
        List<MessageDto> messageDtos = new ArrayList<>();

        for (MessageEntity messageEntity : messageEntities) {
            MessageDto messageDto = new MessageDto();
            messageDto.setMessage(messageEntity.getMessage());
            messageDto.setSender(messageEntity.getSender().getUsername());
            messageDto.setReceiver(messageEntity.getReceiver().getUsername());
            messageDto.setSendDate(messageEntity.getTimestamp());
            messageDto.setRead(messageEntity.isRead());
            messageDtos.add(messageDto);
        }

        return messageDtos;
    }

    public void markMessagesAsRead(UserEntity user1, UserEntity user2) {
        List<MessageEntity> messageEntities = messageDao.findMessageByUser(user1, user2);

        for (MessageEntity messageEntity : messageEntities) {
            if (messageEntity.getReceiver().getUsername().equals(user1.getUsername()) && !messageEntity.isRead()) {
                messageEntity.setRead(true);
                messageDao.update(messageEntity);
            }
        }
    }

    public Session isReceiverloggedIn(UserEntity receiver, UserEntity sender) {
        String conversationId = receiver.getToken() + sender.getUsername();
        Map<String, jakarta.websocket.Session> sessions = getSessions();
        return sessions.get(conversationId);
    }
}