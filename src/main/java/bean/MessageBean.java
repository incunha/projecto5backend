package bean;

import dao.MessageDao;
import dto.MessageDto;
import entities.MessageEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

            messageDao.createMessage(messageEntity); // Persiste a mensagem no banco de dados
            LOGGER.info("Message sent and saved in the database.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while sending and saving the message: ", e);
        }
    }
    // Método para recuperar todas as mensagens trocadas entre dois usuários
    public List<MessageDto> getMessagesBetweenUsers(UserEntity user1, UserEntity user2) {
        List<MessageEntity> messageEntities = messageDao.findMessageByUser(user1, user2);
        List<MessageDto> messageDtos = new ArrayList<>();

        // Converte as entidades de mensagem em DTOs de mensagem
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

    // Método para marcar uma mensagem como lida
    public void markMessagesAsRead(UserEntity user1, UserEntity user2) {
        List<MessageEntity> messageEntities = messageDao.findMessageByUser(user1, user2);

        for (MessageEntity messageEntity : messageEntities) {
            if (messageEntity.getReceiver().getUsername().equals(user1.getUsername()) && !messageEntity.isRead()) {
                messageEntity.setRead(true);
                messageDao.update(messageEntity);
            }
        }
    }

}